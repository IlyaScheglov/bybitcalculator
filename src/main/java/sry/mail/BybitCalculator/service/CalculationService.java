package sry.mail.BybitCalculator.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import sry.mail.BybitCalculator.entity.Chart;
import sry.mail.BybitCalculator.entity.User;
import sry.mail.BybitCalculator.kafka.dto.NotificationType;
import sry.mail.BybitCalculator.kafka.dto.UserNotificationEventDto;
import sry.mail.BybitCalculator.kafka.producer.UserNotificationEventProducer;
import sry.mail.BybitCalculator.model.SymbolCalculatedPercent;
import sry.mail.BybitCalculator.repository.ChartRepository;
import sry.mail.BybitCalculator.repository.UserRepository;
import sry.mail.BybitCalculator.util.AsyncCollectionProcessingUtils;
import sry.mail.BybitCalculator.util.CalculationUtils;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CalculationService {

    private static final String TOP_SYMBOLS_FORMAT = """
            Топ фьючерсов по проценту роста за %s минут: 
            
            %s
            
            Топ фьючерсов по проценту падения за %s минут:
            
            %s
            """;
    private static final String TOP_ELEMENT_FORMAT = "%s.%s - %s процентов\n";

    private final ChartRepository chartRepository;
    private final UserRepository userRepository;

    private final AsyncCollectionProcessingUtils asyncCollectionProcessingUtils;
    private final UserNotificationEventProducer userNotificationEventProducer;

    @Qualifier("asyncExecutor")
    private final Executor executor;

    public void calculateNotifications() {
        var nowDateTime = OffsetDateTime.now();
        var maxMinutes = userRepository.findMaxMinutesPeriod();

        if (maxMinutes.isEmpty()) {
            return;
        }

        var chartsBySymbolMap = chartRepository.findByTimestampIsAfter(nowDateTime.minusMinutes(maxMinutes.get()))
                .stream()
                .collect(Collectors.groupingBy(Chart::getSymbol));
        var activeUsers = userRepository.findByActiveIsTrue();

        var longMinutes = activeUsers.stream()
                .map(User::getLongMinutes)
                .collect(Collectors.toSet());
        var shortMinutes = activeUsers.stream()
                .map(User::getShortMinutes)
                .collect(Collectors.toSet());
        var dumpMinutes = activeUsers.stream()
                .map(User::getDumpMinutes)
                .collect(Collectors.toSet());

        var longCalculationMapFuture = CompletableFuture.supplyAsync(() -> calculatePercentsForEveryMinute(
                longMinutes, chartsBySymbolMap, nowDateTime, false), executor);
        var shortCalculationMapFuture = CompletableFuture.supplyAsync(() -> calculatePercentsForEveryMinute(
                shortMinutes, chartsBySymbolMap, nowDateTime, false), executor);
        var dumpCalculationMapFuture = CompletableFuture.supplyAsync(() -> calculatePercentsForEveryMinute(
                dumpMinutes, chartsBySymbolMap, nowDateTime, true), executor);


        asyncCollectionProcessingUtils.runForEachElementAsync(activeUsers,
                user -> sendUserNotificationIfThereAreAppropriateSignals(user, longCalculationMapFuture.join(),
                        shortCalculationMapFuture.join(), dumpCalculationMapFuture.join()));
    }

    public String getTopSymbolsByPumpsAndDumpsForMinutes(Integer minutes) {
        var topTime = OffsetDateTime.now().minusMinutes(minutes);
        var chartsBySymbolMap = chartRepository.findByTimestampIsAfter(OffsetDateTime.now().minusMinutes(minutes))
                .stream()
                .collect(Collectors.groupingBy(Chart::getSymbol));

        var symbolsPercents = getEverySymbolPercentsForMinutes(chartsBySymbolMap, topTime, false);

        return String.format(TOP_SYMBOLS_FORMAT, minutes, calculateTopElementStrByCharts(symbolsPercents, false),
                minutes, calculateTopElementStrByCharts(symbolsPercents, true));
    }

    private String calculateTopElementStrByCharts(List<SymbolCalculatedPercent> symbolsPercents, boolean reversed) {
        var comparator = reversed
                ? Comparator.comparing(SymbolCalculatedPercent::getPercent, Comparator.nullsLast(Comparator.reverseOrder()))
                : Comparator.comparing(SymbolCalculatedPercent::getPercent, Comparator.nullsLast(Comparator.naturalOrder()));
        return symbolsPercents.stream()
                .sorted(comparator)
                .limit(5)
                .map(symbolInfo -> String.format(TOP_ELEMENT_FORMAT, 1, symbolInfo.getSymbol(), symbolInfo.getPercent()))
                .collect(Collectors.joining());
    }

    private Map<Integer, List<SymbolCalculatedPercent>> calculatePercentsForEveryMinute(Set<Integer> minutes,
                                                                                        Map<String, List<Chart>> chartsMap,
                                                                                        OffsetDateTime nowDateTime,
                                                                                        boolean reverted) {
        return asyncCollectionProcessingUtils.supplyForEachElementAsync(minutes,
                minute -> Map.entry(minute, getEverySymbolPercentsForMinutes(chartsMap, nowDateTime.minusMinutes(minute), reverted)))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private void sendUserNotificationIfThereAreAppropriateSignals(User user,
                                                                  Map<Integer, List<SymbolCalculatedPercent>> longMinutePercentsMap,
                                                                  Map<Integer, List<SymbolCalculatedPercent>> shortMinutePercentsMap,
                                                                  Map<Integer, List<SymbolCalculatedPercent>> dumpMinutePercentsMap) {
        longMinutePercentsMap.getOrDefault(user.getLongMinutes(), Collections.emptyList()).stream()
                .filter(symbolPercent -> symbolPercent.getPercent().compareTo(user.getLongPercent()) > -1)
                .forEach(symbolPercent -> sendMessage(user, symbolPercent, NotificationType.LONG));
        shortMinutePercentsMap.getOrDefault(user.getShortMinutes(), Collections.emptyList()).stream()
                .filter(symbolPercent -> symbolPercent.getPercent().compareTo(user.getShortPercent()) > -1)
                .forEach(symbolPercent -> sendMessage(user, symbolPercent, NotificationType.SHORT));
        dumpMinutePercentsMap.getOrDefault(user.getDumpMinutes(), Collections.emptyList()).stream()
                .filter(symbolPercent -> symbolPercent.getPercent().compareTo(user.getDumpPercent()) > -1)
                .forEach(symbolPercent -> sendMessage(user, symbolPercent, NotificationType.DUMP));
    }

    private List<SymbolCalculatedPercent> getEverySymbolPercentsForMinutes(Map<String, List<Chart>> chartsMap,
                                                                           OffsetDateTime afterTime, boolean revertPercents) {

        return chartsMap.entrySet().stream()
                .map(chartEntry -> {
                    var chartsAfterTime = chartEntry.getValue().stream()
                            .filter(chart -> !chart.getTimestamp().isBefore(afterTime))
                            .toList();
                    var percentOriginal = CalculationUtils
                            .calculateDiffInPercents(findFirstPrice(chartsAfterTime), findLastPrice(chartsAfterTime));
                    var percent = revertPercents ? percentOriginal.multiply(BigDecimal.valueOf(-1)) : percentOriginal;

                    return SymbolCalculatedPercent.builder()
                            .symbol(chartEntry.getKey())
                            .percent(percent)
                            .build();
                })
                .toList();
    }

    private BigDecimal findFirstPrice(List<Chart> charts) {
        return charts.stream()
                .min(Comparator.comparing(Chart::getTimestamp))
                .map(Chart::getPrice)
                .orElse(BigDecimal.ZERO);
    }

    private BigDecimal findLastPrice(List<Chart> charts) {
        return charts.stream()
                .max(Comparator.comparing(Chart::getTimestamp))
                .map(Chart::getPrice)
                .orElse(BigDecimal.ZERO);
    }

    private void sendMessage(User user, SymbolCalculatedPercent symbolCalculatedPercent, NotificationType type) {
        var message = UserNotificationEventDto.builder()
                .tgId(user.getTgId())
                .symbol(symbolCalculatedPercent.getSymbol())
                .percent(symbolCalculatedPercent.getPercent())
                .type(type)
                .build();
        userNotificationEventProducer.sendUserNotificationEvent(message);
    }
}

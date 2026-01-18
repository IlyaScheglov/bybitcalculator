package sry.mail.BybitCalculator.service;

import lombok.RequiredArgsConstructor;
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
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CalculationService {

    private final ChartRepository chartRepository;
    private final UserRepository userRepository;

    private final AsyncCollectionProcessingUtils asyncCollectionProcessingUtils;
    private final UserNotificationEventProducer userNotificationEventProducer;

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

        asyncCollectionProcessingUtils.runForEachElementAsync(activeUsers,
                user -> sendUserNotificationIfThereAreAppropriateSignals(user, chartsBySymbolMap, nowDateTime));
    }

    private void sendUserNotificationIfThereAreAppropriateSignals(User user, Map<String, List<Chart>> chartsMap,
                                                                  OffsetDateTime nowDateTime) {
        var longPercents = getEverySymbolPercentsForMinutes(chartsMap, nowDateTime.minusMinutes(user.getLongMinutes()), false);
        var shortPercents = getEverySymbolPercentsForMinutes(chartsMap, nowDateTime.minusMinutes(user.getShortMinutes()), false);
        var dumpPercents = getEverySymbolPercentsForMinutes(chartsMap, nowDateTime.minusMinutes(user.getDumpMinutes()), true);

        longPercents.stream()
                .filter(symbolPercent -> symbolPercent.getPercent().compareTo(user.getLongPercent()) > -1)
                .forEach(symbolPercent -> sendMessage(user, symbolPercent, NotificationType.LONG));
        shortPercents.stream()
                .filter(symbolPercent -> symbolPercent.getPercent().compareTo(user.getShortPercent()) > -1)
                .forEach(symbolPercent -> sendMessage(user, symbolPercent, NotificationType.SHORT));
        dumpPercents.stream()
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

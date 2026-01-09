package sry.mail.BybitCalculator.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sry.mail.BybitCalculator.entity.Chart;
import sry.mail.BybitCalculator.entity.Purchase;
import sry.mail.BybitCalculator.entity.User;
import sry.mail.BybitCalculator.kafka.dto.BybitParsedEventDto;
import sry.mail.BybitCalculator.kafka.dto.NotificationType;
import sry.mail.BybitCalculator.kafka.dto.UserNotificationEventDto;
import sry.mail.BybitCalculator.kafka.producer.UserNotificationEventProducer;
import sry.mail.BybitCalculator.mapper.ChartMapper;
import sry.mail.BybitCalculator.model.SymbolCalculatedDumpPercent;
import sry.mail.BybitCalculator.repository.ChartRepository;
import sry.mail.BybitCalculator.repository.PurchaseRepository;
import sry.mail.BybitCalculator.repository.UserRepository;
import sry.mail.BybitCalculator.util.AsyncCollectionProcessingUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CalculationService {

    private final ChartRepository chartRepository;
    private final ChartMapper chartMapper;
    private final UserRepository userRepository;
    private final PurchaseRepository purchaseRepository;

    private final AsyncCollectionProcessingUtils asyncCollectionProcessingUtils;
    private final UserNotificationEventProducer userNotificationEventProducer;

    @Transactional
    public void saveNewChart(BybitParsedEventDto bybitEvent) {
        chartRepository.save(chartMapper.mapEventDtoToChartEntity(bybitEvent));
    }

    @Transactional
    public void deleteOldCharts() {
        chartRepository.deleteChartsWhereTimestampIsBefore(OffsetDateTime.now().minusDays(1));
    }

    public void calculateDumps() {
        var fiveMinutesAgoTimestamp = OffsetDateTime.now().minusMinutes(5);
        var chartsBySymbolMap = chartRepository.findByTimestampIsAfter(fiveMinutesAgoTimestamp)
                .stream()
                .collect(Collectors.groupingBy(Chart::getSymbol));

        var dumpPercent = getEverySymbolDumpPercents(chartsBySymbolMap);
        var activeUsers = userRepository.findByActiveIsTrue();

        asyncCollectionProcessingUtils.runForEachElementAsync(activeUsers,
                user -> sendUserNotificationIfThereAreAppropriateDumps(user, dumpPercent));
    }

    public void calculateReadyToSell() {
        var purchases = purchaseRepository.findAll();
        asyncCollectionProcessingUtils.runForEachElementAsync(purchases, this::sendUserNotificationIfSymbolIsReadyToSell);
    }

    private void sendUserNotificationIfThereAreAppropriateDumps(User user, List<SymbolCalculatedDumpPercent> dumpPercents) {
        dumpPercents.stream()
                .filter(dumpPercent ->
                        dumpPercent.getDumpPercent().compareTo(user.getMinPercentOfDump()) > -1)
                .forEach(dumpPercent ->
                        userNotificationEventProducer.sendUserNotificationEvent(
                                UserNotificationEventDto.builder()
                                        .tgId(user.getTgId())
                                        .symbol(dumpPercent.getSymbol())
                                        .type(NotificationType.BUY)
                                        .build()));
    }

    private void sendUserNotificationIfSymbolIsReadyToSell(Purchase purchase) {
        var lastChartOfSymbol = chartRepository
                .findTopBySymbolOrderByTimestampDesc(purchase.getSymbol());

        if (lastChartOfSymbol.isPresent()) {
            var userInfo = purchase.getUser();
            var buyPrice = purchase.getBuyPrice();

            var incomePercent = lastChartOfSymbol.get().getPrice().subtract(buyPrice)
                    .divide(buyPrice, 100, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(10, RoundingMode.HALF_UP);

            if (incomePercent.compareTo(userInfo.getMinPercentOfIncome()) > -1) {
                userNotificationEventProducer.sendUserNotificationEvent(UserNotificationEventDto.builder()
                        .tgId(purchase.getUser().getTgId())
                        .symbol(purchase.getSymbol())
                        .type(NotificationType.SELL)
                        .build());
            }
        }
    }

    private List<SymbolCalculatedDumpPercent> getEverySymbolDumpPercents(Map<String, List<Chart>> chartsMap) {
        return chartsMap.entrySet().stream()
                .map(chartEntry -> SymbolCalculatedDumpPercent.builder()
                        .symbol(chartEntry.getKey())
                        .dumpPercent(findDumpPercentOfSymbol(chartEntry.getValue()))
                        .build())
                .toList();
    }

    private BigDecimal findDumpPercentOfSymbol(List<Chart> charts) {
        var firstPrice = findFirstPrice(charts);
        var lastPrice = findLastPrice(charts);
        return lastPrice.subtract(firstPrice)
                .divide(firstPrice, 100, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(-100))
                .setScale(10, RoundingMode.HALF_UP);
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
}

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
import sry.mail.BybitCalculator.model.SymbolCalculatedPumpPercent;
import sry.mail.BybitCalculator.repository.ChartRepository;
import sry.mail.BybitCalculator.repository.PurchaseRepository;
import sry.mail.BybitCalculator.repository.UserRepository;
import sry.mail.BybitCalculator.util.AsyncCollectionProcessingUtils;
import sry.mail.BybitCalculator.util.TransactionManipulator;

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
    private final TransactionManipulator transactionManipulator;
    private final UserNotificationEventProducer userNotificationEventProducer;

    @Transactional
    public void saveNewChart(BybitParsedEventDto bybitEvent) {
        chartRepository.save(chartMapper.mapEventDtoToChartEntity(bybitEvent));
    }

    @Transactional
    public void deleteOldCharts() {
        chartRepository.deleteChartsWhereTimestampIsBefore(OffsetDateTime.now().minusDays(1));
    }

    public void calculatePumps() {
        var fiveMinutesAgoTimestamp = OffsetDateTime.now().minusMinutes(5);
        var chartsBySymbolMap = chartRepository.findByTimestampIsAfter(fiveMinutesAgoTimestamp)
                .stream()
                .collect(Collectors.groupingBy(Chart::getSymbol));

        var pumpPercents = getEverySymbolPumpPercents(chartsBySymbolMap);
        var activeUsers = userRepository.findByActiveIsTrue();

        asyncCollectionProcessingUtils.runForEachElementAsync(activeUsers,
                user -> sendUserNotificationIfThereAreAppropriatePumps(user, pumpPercents));
    }

    public void calculateReadyToSell() {
        var purchases = purchaseRepository.findByCreateTimestampIsBefore(OffsetDateTime.now().minusMinutes(3));
        asyncCollectionProcessingUtils.runForEachElementAsync(purchases,
                purchase -> transactionManipulator.doInNewTransaction(
                        () -> sendUserNotificationIfSymbolIsReadyToSell(purchase)
                ));
    }

    private void sendUserNotificationIfThereAreAppropriatePumps(User user, List<SymbolCalculatedPumpPercent> pumpPercents) {
        pumpPercents.stream()
                .filter(pumpPercent ->
                        pumpPercent.getPumpPercent().compareTo(user.getMinPercentOfPush()) > -1)
                .forEach(pumpPercent ->
                        userNotificationEventProducer.sendUserNotificationEvent(
                                UserNotificationEventDto.builder()
                                        .tgId(user.getTgId())
                                        .symbol(pumpPercent.getSymbol())
                                        .type(NotificationType.BUY)
                                        .build()));
    }

    private Purchase sendUserNotificationIfSymbolIsReadyToSell(Purchase purchase) {
        var chartsAfterLastUpdate = chartRepository
                .findBySymbolAndTimestampIsAfter(purchase.getSymbol(), purchase.getUpdateTimestamp());

        var currentPrice = findFirstPrice(chartsAfterLastUpdate, purchase.getMaxPrice());
        var atrAmount = purchase.getAtrAmount();
        var atrCount = purchase.getAtrCount();

        for (var chart : chartsAfterLastUpdate) {
            var nowPrice = chart.getPrice();
            var volatility = nowPrice.subtract(currentPrice).abs();

            currentPrice = nowPrice;
            atrAmount = atrAmount.add(volatility);
            atrCount++;
        }

        var lastPrice = findLastPrice(chartsAfterLastUpdate, purchase.getMaxPrice());
        var maxPrice = chartsAfterLastUpdate.stream()
                .map(Chart::getPrice)
                .max(Comparator.naturalOrder())
                .orElse(purchase.getMaxPrice()).max(purchase.getMaxPrice());
        var lowLevelSubtractor = atrCount != 0
                ? atrAmount.divide(BigDecimal.valueOf(atrCount), 10, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(1.5))
                : BigDecimal.ZERO;
        var lowLevel = maxPrice.subtract(lowLevelSubtractor);

        if (lastPrice.compareTo(lowLevel) < 1) {
            userNotificationEventProducer.sendUserNotificationEvent(UserNotificationEventDto.builder()
                    .tgId(purchase.getUser().getTgId())
                    .symbol(purchase.getSymbol())
                    .type(NotificationType.SELL)
                    .build());
        }
        return purchaseRepository.save(purchase.setMaxPrice(maxPrice).setAtrAmount(atrAmount).setAtrCount(atrCount)
                .setUpdateTimestamp(OffsetDateTime.now()));
    }

    private List<SymbolCalculatedPumpPercent> getEverySymbolPumpPercents(Map<String, List<Chart>> chartsMap) {
        return chartsMap.entrySet().stream()
                .map(chartEntry -> SymbolCalculatedPumpPercent.builder()
                        .symbol(chartEntry.getKey())
                        .pumpPercent(findPumpPercentOfSymbol(chartEntry.getValue()))
                        .build())
                .toList();
    }

    private BigDecimal findPumpPercentOfSymbol(List<Chart> charts) {
        var firstPrice = findFirstPrice(charts, BigDecimal.ZERO);
        var lastPrice = findLastPrice(charts, BigDecimal.ZERO);
        return lastPrice.subtract(firstPrice)
                .divide(firstPrice, 100, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(10, RoundingMode.HALF_UP);
    }

    private BigDecimal findFirstPrice(List<Chart> charts, BigDecimal defaultValue) {
        return charts.stream()
                .min(Comparator.comparing(Chart::getTimestamp))
                .map(Chart::getPrice)
                .orElse(defaultValue);
    }

    private BigDecimal findLastPrice(List<Chart> charts, BigDecimal defaultValue) {
        return charts.stream()
                .max(Comparator.comparing(Chart::getTimestamp))
                .map(Chart::getPrice)
                .orElse(defaultValue);
    }
}

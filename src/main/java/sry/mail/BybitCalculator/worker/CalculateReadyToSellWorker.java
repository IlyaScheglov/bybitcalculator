package sry.mail.BybitCalculator.worker;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import sry.mail.BybitCalculator.service.CalculationService;

@Slf4j
@Component
@ConditionalOnProperty(value = "scheduler.calculate-ready-to-sell-worker.enabled", havingValue = "true")
@RequiredArgsConstructor
public class CalculateReadyToSellWorker {

    private final CalculationService calculationService;

    @Scheduled(cron = "${scheduler.calculate-ready-to-sell-worker.cron}")
    @SchedulerLock(name = "CalculateReadyToSellWorker",
            lockAtLeastFor = "${scheduler.calculate-ready-to-sell-worker.lock-at-least}",
            lockAtMostFor = "${scheduler.calculate-ready-to-sell-worker.lock-at-most}")
    public void calculateReadyToSell() {
        log.info("Calculate ready to sell worker started");
        calculationService.calculateReadyToSell();
        log.info("Calculate ready to sell worker finished");
    }
}

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
@ConditionalOnProperty(value = "scheduler.calculate-pumps-worker.enabled", havingValue = "true")
@RequiredArgsConstructor
public class CalculatePumpsWorker {

    private final CalculationService calculationService;

    @Scheduled(cron = "${scheduler.calculate-pumps-worker.cron}")
    @SchedulerLock(name = "CalculatePumpsWorker",
            lockAtLeastFor = "${scheduler.calculate-pumps-worker.lock-at-least}",
            lockAtMostFor = "${scheduler.calculate-pumps-worker.lock-at-most}")
    public void calculatePumps() {
        log.info("Calculate pump worker started");
        calculationService.calculatePumps();
        log.info("Calculate pump worker finished");
    }
}

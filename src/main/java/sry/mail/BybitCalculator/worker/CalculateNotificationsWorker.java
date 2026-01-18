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
@ConditionalOnProperty(value = "scheduler.calculate-dumps-worker.enabled", havingValue = "true")
@RequiredArgsConstructor
public class CalculateNotificationsWorker {

    private final CalculationService calculationService;

    @Scheduled(cron = "${scheduler.calculate-dumps-worker.cron}")
    @SchedulerLock(name = "CalculateDumpsWorker",
            lockAtLeastFor = "${scheduler.calculate-dumps-worker.lock-at-least}",
            lockAtMostFor = "${scheduler.calculate-dumps-worker.lock-at-most}")
    public void calculateDumps() {
        log.info("Calculate dump worker started");
        calculationService.calculateNotifications();
        log.info("Calculate dump worker finished");
    }
}

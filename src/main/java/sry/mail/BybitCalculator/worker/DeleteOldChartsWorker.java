package sry.mail.BybitCalculator.worker;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import sry.mail.BybitCalculator.service.CalculationService;
import sry.mail.BybitCalculator.service.ChartService;

@Slf4j
@Component
@ConditionalOnProperty(value = "scheduler.delete-old-charts-worker.enabled", havingValue = "true")
@RequiredArgsConstructor
public class DeleteOldChartsWorker {

    private final ChartService chartService;

    @Scheduled(cron = "${scheduler.delete-old-charts-worker.cron}")
    @SchedulerLock(name = "DeleteOldChartsWorker",
            lockAtLeastFor = "${scheduler.delete-old-charts-worker.lock-at-least}",
            lockAtMostFor = "${scheduler.delete-old-charts-worker.lock-at-most}")
    public void deleteOldCharts() {
        log.info("Delete old charts worker started");
        chartService.deleteOldCharts();
        log.info("Delete old charts worker finished");
    }
}

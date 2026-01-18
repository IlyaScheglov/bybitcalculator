package sry.mail.BybitCalculator.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sry.mail.BybitCalculator.kafka.dto.BybitParsedEventDto;
import sry.mail.BybitCalculator.mapper.ChartMapper;
import sry.mail.BybitCalculator.repository.ChartRepository;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class ChartService {

    private final ChartRepository chartRepository;
    private final ChartMapper chartMapper;

    @Transactional
    public void saveNewChart(BybitParsedEventDto bybitEvent) {
        chartRepository.save(chartMapper.mapEventDtoToChartEntity(bybitEvent));
    }

    @Transactional
    public void deleteOldCharts() {
        chartRepository.deleteChartsWhereTimestampIsBefore(OffsetDateTime.now().minusDays(1));
    }
}

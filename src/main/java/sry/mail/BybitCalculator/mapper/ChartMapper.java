package sry.mail.BybitCalculator.mapper;

import org.mapstruct.Mapper;
import sry.mail.BybitCalculator.entity.Chart;
import sry.mail.BybitCalculator.kafka.dto.BybitParsedEventDto;

@Mapper(componentModel = "spring")
public interface ChartMapper {

    Chart mapEventDtoToChartEntity(BybitParsedEventDto eventDto);
}

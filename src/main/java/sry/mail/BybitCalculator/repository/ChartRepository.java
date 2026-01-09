package sry.mail.BybitCalculator.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sry.mail.BybitCalculator.entity.Chart;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChartRepository extends JpaRepository<Chart, UUID> {

    Optional<Chart> findTopBySymbolOrderByTimestampDesc(String symbol);

    @Query("""
            select c 
            from Chart c 
            where c.timestamp >= :timestampAfter 
            """)
    List<Chart> findByTimestampIsAfter(@Param("timestampAfter") OffsetDateTime timestampAfter);

    @Modifying
    @Query("""
            delete from Chart c 
            where c.timestamp <= :timestampBefore
            """)
    void deleteChartsWhereTimestampIsBefore(@Param("timestampBefore") OffsetDateTime timestampBefore);
}

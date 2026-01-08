package sry.mail.BybitCalculator.repository;

import org.apache.kafka.common.protocol.types.Field;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sry.mail.BybitCalculator.entity.Purchase;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PurchaseRepository extends JpaRepository<Purchase, UUID> {

    @Query(nativeQuery = true, value = """
            select p.* 
            from purchases p 
            join users u 
            on p.user_id = u.id 
            where p.symbol = :symbol 
            and u.tg_id = :tgId
            """)
    Optional<Purchase> findBySymbolAndUserTgId(@Param("symbol") String symbol,
                                               @Param("tgId") String tgId);;

    @Query("""
            select p 
            from Purchase p 
            join fetch p.user 
            where p.createTimestamp <= :maxCreateTimestamp
            """)
    List<Purchase> findByCreateTimestampIsBefore(@Param("maxCreateTimestamp") OffsetDateTime maxCreateTimestamp);
}

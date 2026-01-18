package sry.mail.BybitCalculator.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import sry.mail.BybitCalculator.entity.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    @EntityGraph(attributePaths = "purchases")
    Optional<User> findByTgId(String tgId);

    List<User> findByActiveIsTrue();

    @Query(nativeQuery = true, value = """
            select greatest(long_minutes, short_minutes, dump_minutes) 
            from users
            """)
    Optional<Integer> findMaxMinutesPeriod();
}

package sry.mail.BybitCalculator.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import sry.mail.BybitCalculator.entity.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    @EntityGraph(attributePaths = "purchases")
    Optional<User> findByTgId(String tgId);

    List<User> findByActiveIsTrue();
}

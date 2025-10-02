// imbuy.backend.repository.UserRepository.java
package imbuy.backend.repository;

import imbuy.backend.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);

    @Query("SELECT u.balance FROM User u WHERE u.id = :userId")
    Optional<java.math.BigDecimal> findBalanceById(@Param("userId") Long userId);
}
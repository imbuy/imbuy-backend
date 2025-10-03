// imbuy.backend.repository.FavoriteRepository.java
package imbuy.backend.repository;

import imbuy.backend.domain.Favorite;
import imbuy.backend.domain.FavoriteId;
import imbuy.backend.domain.Lot;
import imbuy.backend.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, FavoriteId> {
    Optional<Favorite> findByUserAndLot(User user, Lot lot);
    Page<Favorite> findByUser(User user, Pageable pageable);
    boolean existsByUserAndLot(User user, Lot lot);

    @Query("SELECT f.lot FROM Favorite f WHERE f.user.id = :userId")
    Page<Lot> findFavoriteLotsByUserId(@Param("userId") Long userId, Pageable pageable);

    void deleteByUserAndLot(User user, Lot lot);

    @Query("SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END FROM Favorite f WHERE f.user.id = :userId AND f.lot.id = :lotId")
    boolean existsByUserIdAndLotId(@Param("userId") Long userId, @Param("lotId") Long lotId);
}
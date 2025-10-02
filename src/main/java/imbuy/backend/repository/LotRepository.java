// imbuy.backend.repository.LotRepository.java
package imbuy.backend.repository;

import imbuy.backend.domain.Lot;
import imbuy.backend.enums.LotStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface LotRepository extends JpaRepository<Lot, Long> {
    Page<Lot> findByStatus(LotStatus status, Pageable pageable);
    Page<Lot> findByCategoryId(Long categoryId, Pageable pageable);
    Page<Lot> findByOwnerId(Long ownerId, Pageable pageable);
    Page<Lot> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    @Query("SELECT l FROM Lot l WHERE " +
            "(:title IS NULL OR LOWER(l.title) LIKE LOWER(CONCAT('%', :title, '%'))) AND " +
            "(:status IS NULL OR l.status = :status) AND " +
            "(:categoryId IS NULL OR l.category.id = :categoryId) AND " +
            "(:ownerId IS NULL OR l.owner.id = :ownerId)")
    Page<Lot> findByFilters(@Param("title") String title,
                            @Param("status") LotStatus status,
                            @Param("categoryId") Long categoryId,
                            @Param("ownerId") Long ownerId,
                            Pageable pageable);

    @Query("SELECT l FROM Lot l WHERE l.status = 'ACTIVE' AND l.endDate <= :now")
    List<Lot> findExpiredActiveLots(@Param("now") LocalDateTime now);

    @Query("SELECT l FROM Lot l WHERE l.status = 'ACTIVE' ORDER BY l.currentPrice DESC")
    Page<Lot> findActiveLotsOrderByPrice(Pageable pageable);

    Optional<Lot> findByIdAndStatus(Long id, LotStatus status);

    @Query("SELECT COUNT(l) FROM Lot l WHERE l.category.id = :categoryId")
    Long countByCategoryId(@Param("categoryId") Long categoryId);
}

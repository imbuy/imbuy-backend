package imbuy.backend.repository;

import imbuy.backend.domain.Bid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BidRepository extends JpaRepository<Bid, Long> {
    Page<Bid> findByLotIdOrderByCreatedAtDesc(Long lotId, Pageable pageable);
    List<Bid> findByLotIdOrderByAmountDesc(Long lotId);
    Optional<Bid> findTopByLotIdOrderByAmountDesc(Long lotId);

    @Query("SELECT COUNT(b) FROM Bid b WHERE b.lot.id = :lotId")
    Long countByLotId(@Param("lotId") Long lotId);

    @Query("SELECT b FROM Bid b WHERE b.lot.id = :lotId AND b.bidder.id = :bidderId")
    List<Bid> findByLotIdAndBidderId(@Param("lotId") Long lotId, @Param("bidderId") Long bidderId);
}

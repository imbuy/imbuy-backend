// imbuy.backend.scheduler.AuctionScheduler.java
package imbuy.backend.scheduler;

import imbuy.backend.domain.Bid;
import imbuy.backend.domain.Lot;
import imbuy.backend.enums.LotStatus;
import imbuy.backend.repository.BidRepository;
import imbuy.backend.repository.LotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuctionScheduler {

    private final LotRepository lotRepository;
    private final BidRepository bidRepository;

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void completeExpiredAuctions() {
        log.info("Checking for expired auctions...");

        List<Lot> expiredLots = lotRepository.findExpiredActiveLots(LocalDateTime.now());

        for (Lot lot : expiredLots) {
            try {
                completeAuction(lot);
            } catch (Exception e) {
                log.error("Error completing auction for lot {}: {}", lot.getId(), e.getMessage());
            }
        }

        log.info("Completed checking expired auctions. Processed {} lots.", expiredLots.size());
    }

    private void completeAuction(Lot lot) {
        log.info("Completing auction for lot: {}", lot.getId());

        // Находим победившую ставку
        List<Bid> winningBids = bidRepository.findByLotIdOrderByAmountDesc(lot.getId());

        if (winningBids.isEmpty()) {
            // Нет ставок - лот завершается без победителя
            lot.setStatus(LotStatus.COMPLETED);
            log.info("Lot {} completed with no bids", lot.getId());
        } else {
            // Есть победитель
            Bid winningBid = winningBids.get(0);
            lot.setStatus(LotStatus.COMPLETED);
            //  можно добавить логику уведомления победителя и продавца
            log.info("Lot {} completed. Winner: {}, Winning bid: {}",
                    lot.getId(), winningBid.getBidder().getUsername(), winningBid.getAmount());
        }

        lotRepository.save(lot);
    }
}
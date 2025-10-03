package imbuy.backend.service;

import imbuy.backend.domain.Bid;
import imbuy.backend.domain.Lot;
import imbuy.backend.domain.User;
import imbuy.backend.dto.BidDto;
import imbuy.backend.dto.CreateBidDto;
import imbuy.backend.dto.PageResponse;
import imbuy.backend.enums.LotStatus;
import imbuy.backend.repository.BidRepository;
import imbuy.backend.repository.CategoryRepository;
import imbuy.backend.repository.LotRepository;
import imbuy.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class BidService {

    private final BidRepository bidRepository;
    private final LotRepository lotRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public PageResponse<BidDto> getBidsByLotId(Long lotId, Pageable pageable) {
        if (!lotRepository.existsById(lotId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Lot not found");
        }

        Page<Bid> bids = bidRepository.findByLotIdOrderByCreatedAtDesc(lotId, pageable);
        return PageResponse.of(bids.map(this::convertToDto));
    }

    @Transactional
    public BidDto placeBid(Long lotId, CreateBidDto createBidDto, Long bidderId) {
        Lot lot = lotRepository.findById(lotId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lot not found"));

        User bidder = userRepository.findById(bidderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        validateBid(lot, createBidDto.getAmount(), bidderId);

        Bid bid = new Bid(lot, bidder, createBidDto.getAmount());
        bid = bidRepository.save(bid);

        lot.setCurrentPrice(createBidDto.getAmount());
        lotRepository.save(lot);

        return convertToDto(bid);
    }

    private void validateBid(Lot lot, BigDecimal amount, Long bidderId) {
        if (lot.getStatus() != LotStatus.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Lot is not active");
        }

        if (lot.getOwner().getId().equals(bidderId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You cannot bid on your own lot");
        }

        BigDecimal minBid = lot.getCurrentPrice().add(lot.getBidStep());
        if (amount.compareTo(minBid) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("Bid must be at least %.2f", minBid));
        }

        if (bidderId != null) {
            // Здесь должна быть проверка баланса пользователя
        }
    }

    @Transactional(readOnly = true)
    public BidDto getWinningBid(Long lotId) {
        return bidRepository.findTopByLotIdOrderByAmountDesc(lotId)
                .map(this::convertToDto)
                .orElse(null);
    }

    private BidDto convertToDto(Bid bid) {
        BidDto dto = new BidDto();
        dto.setId(bid.getId());
        dto.setAmount(bid.getAmount());
        dto.setBidderId(bid.getBidder().getId());
        dto.setBidderUsername(bid.getBidder().getUsername());
        dto.setCreatedAt(bid.getCreatedAt());
        return dto;
    }
}
package imbuy.backend.service;

import imbuy.backend.domain.Category;
import imbuy.backend.domain.Lot;
import imbuy.backend.domain.User;
import imbuy.backend.dto.*;
import imbuy.backend.enums.LotStatus;
import imbuy.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LotService {

    private final LotRepository lotRepository;
    private final UserRepository userRepository;
    private final BidRepository bidRepository;
    private final FavoriteRepository favoriteRepository;
    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public PageResponse<LotDto> getLots(LotFilterDto filter, Pageable pageable, Long currentUserId) {
        Page<Lot> lots;

        if (filter != null && hasFilters(filter)) {
            lots = lotRepository.findByFilters(
                    filter.getTitle(),
                    filter.getStatus(),
                    filter.getCategoryId(),
                    filter.getOwnerId(),
                    pageable
            );
        } else if (filter != null && Boolean.TRUE.equals(filter.getActiveOnly())) {
            lots = lotRepository.findByStatus(LotStatus.ACTIVE, pageable);
        } else {
            lots = lotRepository.findAll(pageable);
        }

        return PageResponse.of(lots.map(lot -> convertToDto(lot, currentUserId)));
    }

    @Transactional(readOnly = true)
    public PageResponse<LotDto> getLotsWithTotalCount(LotFilterDto filter, Pageable pageable, Long currentUserId) {
        Page<Lot> lots = lotRepository.findByFilters(
                filter != null ? filter.getTitle() : null,
                filter != null ? filter.getStatus() : null,
                filter != null ? filter.getCategoryId() : null,
                filter != null ? filter.getOwnerId() : null,
                pageable
        );

        return PageResponse.of(lots.map(lot -> convertToDto(lot, currentUserId)));
    }

    @Transactional(readOnly = true)
    public LotDto getLotById(Long id, Long currentUserId) {
        Lot lot = lotRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lot not found"));
        return convertToDto(lot, currentUserId);
    }

    @Transactional
    public LotDto createLot(CreateLotDto createLotDto, Long ownerId) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Lot lot = new Lot();
        lot.setTitle(createLotDto.getTitle());
        lot.setDescription(createLotDto.getDescription());
        lot.setStartPrice(createLotDto.getStartPrice());
        lot.setCurrentPrice(createLotDto.getStartPrice());
        lot.setBidStep(createLotDto.getBidStep());
        lot.setOwner(owner);
        lot.setStatus(LotStatus.PENDING_APPROVAL); // или DRAFT в зависимости от бизнес-логики

        if (createLotDto.getCategoryId() != null) {
            Category category = categoryRepository.findById(createLotDto.getCategoryId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));
            lot.setCategory(category);
        }

        lot.setStartDate(createLotDto.getStartDate() != null ? createLotDto.getStartDate() : LocalDateTime.now());
        lot.setEndDate(createLotDto.getEndDate());

        lot = lotRepository.save(lot);
        return convertToDto(lot, ownerId);
    }

    @Transactional
    public LotDto updateLot(Long id, UpdateLotDto updateLotDto, Long currentUserId) {
        Lot lot = lotRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lot not found"));

        if (!lot.getOwner().getId().equals(currentUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only update your own lots");
        }

        if (lot.getStatus() != LotStatus.DRAFT && lot.getStatus() != LotStatus.PENDING_APPROVAL) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot update lot in current status");
        }

        if (updateLotDto.getTitle() != null) {
            lot.setTitle(updateLotDto.getTitle());
        }
        if (updateLotDto.getDescription() != null) {
            lot.setDescription(updateLotDto.getDescription());
        }
        if (updateLotDto.getBidStep() != null) {
            lot.setBidStep(updateLotDto.getBidStep());
        }
        if (updateLotDto.getEndDate() != null) {
            lot.setEndDate(updateLotDto.getEndDate());
        }

        lot = lotRepository.save(lot);
        return convertToDto(lot, currentUserId);
    }

    @Transactional
    public void deleteLot(Long id, Long currentUserId) {
        Lot lot = lotRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lot not found"));

        if (!lot.getOwner().getId().equals(currentUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only delete your own lots");
        }

        if (lot.getStatus() == LotStatus.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot delete active lot");
        }

        lotRepository.delete(lot);
    }

    @Transactional
    public LotDto updateLotStatus(Long id, LotStatus status, Long adminId) {
        Lot lot = lotRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lot not found"));

        // Здесь должна быть проверка прав администратора

        lot.setStatus(status);
        if (status == LotStatus.ACTIVE && lot.getStartDate() == null) {
            lot.setStartDate(LocalDateTime.now());
        }

        lot = lotRepository.save(lot);
        return convertToDto(lot, adminId);
    }

    @Transactional
    public void toggleFavorite(Long lotId, Long userId) {
        Lot lot = lotRepository.findById(lotId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lot not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Optional<imbuy.backend.domain.Favorite> existingFavorite = favoriteRepository.findByUserAndLot(user, lot);

        if (existingFavorite.isPresent()) {
            favoriteRepository.delete(existingFavorite.get());
        } else {
            imbuy.backend.domain.Favorite favorite = new imbuy.backend.domain.Favorite(user, lot);
            favoriteRepository.save(favorite);
        }
    }

    private boolean hasFilters(LotFilterDto filter) {
        return filter.getTitle() != null || filter.getStatus() != null ||
                filter.getCategoryId() != null || filter.getOwnerId() != null;
    }

    private LotDto convertToDto(Lot lot, Long currentUserId) {
        LotDto dto = new LotDto();
        dto.setId(lot.getId());
        dto.setTitle(lot.getTitle());
        dto.setDescription(lot.getDescription());
        dto.setStartPrice(lot.getStartPrice());
        dto.setCurrentPrice(lot.getCurrentPrice());
        dto.setBidStep(lot.getBidStep());
        dto.setOwnerId(lot.getOwner().getId());
        dto.setOwnerUsername(lot.getOwner().getUsername());
        if (lot.getCategory() != null) {
            dto.setCategoryId(lot.getCategory().getId());
            dto.setCategoryName(lot.getCategory().getName());
        }
        dto.setStatus(lot.getStatus());
        dto.setStartDate(lot.getStartDate());
        dto.setEndDate(lot.getEndDate());
        dto.setCreatedAt(lot.getCreatedAt());

        dto.setBidCount(Math.toIntExact(bidRepository.countByLotId(lot.getId())));

        if (currentUserId != null) {
            dto.setIsFavorite(favoriteRepository.existsByUserIdAndLotId(currentUserId, lot.getId()));
        }

        return dto;
    }
}
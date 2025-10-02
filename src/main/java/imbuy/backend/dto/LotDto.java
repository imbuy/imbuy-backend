package imbuy.backend.dto;

import imbuy.backend.enums.LotStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class LotDto {
    private Long id;

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @NotNull(message = "Start price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Start price must be greater than 0")
    private BigDecimal startPrice;

    private BigDecimal currentPrice;

    @NotNull(message = "Bid step is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Bid step must be greater than 0")
    private BigDecimal bidStep;

    private Long ownerId;
    private String ownerUsername;
    private Long categoryId;
    private String categoryName;
    private LotStatus status;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime createdAt;
    private Integer bidCount;
    private Boolean isFavorite;
}


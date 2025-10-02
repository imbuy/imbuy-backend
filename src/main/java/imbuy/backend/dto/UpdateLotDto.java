package imbuy.backend.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class UpdateLotDto {
    private String title;
    private String description;
    private BigDecimal bidStep;
    private Long categoryId;
    private LocalDateTime endDate;
}

package imbuy.backend.dto;

import imbuy.backend.enums.LotStatus;
import lombok.Data;

@Data
public class LotFilterDto {
    private String title;
    private LotStatus status;
    private Long categoryId;
    private Long ownerId;
    private Boolean activeOnly = false;
}

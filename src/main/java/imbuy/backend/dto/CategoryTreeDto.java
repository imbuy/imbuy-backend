package imbuy.backend.dto;

import lombok.Data;

import java.util.List;

@Data
public class CategoryTreeDto {
    private List<CategoryDto> categories;
}

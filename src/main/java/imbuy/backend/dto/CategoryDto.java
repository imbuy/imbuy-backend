package imbuy.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CategoryDto {
    private Long id;

    @NotBlank(message = "Category name is required")
    private String name;

    private Long parentId;
    private String parentName;
    private List<CategoryDto> children = new ArrayList<>();
}

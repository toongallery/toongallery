package com.example.toongallery.domain.category.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class CategorySaveRequest {
    @NotBlank
    private String categoryName;
}

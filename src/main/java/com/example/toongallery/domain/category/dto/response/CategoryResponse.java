package com.example.toongallery.domain.category.dto.response;

import com.example.toongallery.domain.category.entity.Category;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CategoryResponse {
    private String categoryName;

    public static CategoryResponse from(Category category) {
        return new CategoryResponse(category.getCategoryName());
    }
}

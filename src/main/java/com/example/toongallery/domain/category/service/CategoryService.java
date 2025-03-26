package com.example.toongallery.domain.category.service;

import com.example.toongallery.domain.category.dto.request.CategorySaveRequest;
import com.example.toongallery.domain.category.dto.response.CategoryResponse;
import com.example.toongallery.domain.category.entity.Category;
import com.example.toongallery.domain.category.repository.CategoryRepository;

import com.example.toongallery.domain.common.exception.BaseException;
import com.example.toongallery.domain.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;
    public CategoryResponse createCategory(CategorySaveRequest request) {
        if(categoryRepository.existsByCategoryName(request.getCategoryName())){
            throw new BaseException(ErrorCode.DUPLICATE_CATEGORY_NAME, request.getCategoryName());
        }
        Category category = Category.of(request.getCategoryName());
        categoryRepository.save(category);
        return new CategoryResponse(category.getCategoryName());
    }
}

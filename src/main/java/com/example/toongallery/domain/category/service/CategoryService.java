package com.example.toongallery.domain.category.service;

import com.example.toongallery.domain.category.dto.request.CategoryRequest;
import com.example.toongallery.domain.category.dto.response.CategoryResponse;
import com.example.toongallery.domain.category.entity.Category;
import com.example.toongallery.domain.category.repository.CategoryRepository;
import com.example.toongallery.domain.common.exception.BaseException;
import com.example.toongallery.domain.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;

    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {

        isDuplicatedName(request.getCategoryName());

        Category category = Category.of(request.getCategoryName().toUpperCase());
        Category savedCategory = categoryRepository.save(category);
        return CategoryResponse.from(savedCategory);
    }

    @Transactional
    public CategoryResponse updateCategory(Long categoryId, CategoryRequest request) {
        Category category = getCategoryEntity(categoryId);

        if (request.getCategoryName() != null && !request.getCategoryName().isBlank()
                && !category.getCategoryName().equals(request.getCategoryName().toUpperCase())) {

            isDuplicatedName(request.getCategoryName());
            category.updateCategoryName(request.getCategoryName().toUpperCase());
        }
        return CategoryResponse.from(category);
    }

    @Transactional
    public void deleteCategory(Long categoryId) {
        Category category = getCategoryEntity(categoryId);
        categoryRepository.delete(category);
    }

    @Transactional(readOnly = true)
    public CategoryResponse getCategory(Long categoryId){
        return CategoryResponse.from(getCategoryEntity(categoryId));
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories(){
        List<Category> categories = categoryRepository.findAll();
        return categories.stream()
                .map(CategoryResponse::from)
                .toList();
    }

    private Category getCategoryEntity(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new BaseException(ErrorCode.CATEGORY_NOT_EXIST, null));
    }

    private void isDuplicatedName(String categoryName) {
        if (categoryRepository.existsByCategoryName(categoryName.toUpperCase())) {
            throw new BaseException(ErrorCode.DUPLICATE_CATEGORY_NAME, categoryName);
        }
    }
}

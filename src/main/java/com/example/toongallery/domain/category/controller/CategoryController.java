package com.example.toongallery.domain.category.controller;

import com.example.toongallery.domain.category.dto.request.CategorySaveRequest;
import com.example.toongallery.domain.category.dto.response.CategoryResponse;
import com.example.toongallery.domain.category.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/categories")
public class CategoryController {
    private final CategoryService categoryService;

    @Secured("ROLE_ADMIN")
    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory(@RequestBody CategorySaveRequest categorySaveRequest){
        CategoryResponse category = categoryService.createCategory(categorySaveRequest);
        return new ResponseEntity<>(category, HttpStatus.OK);
    }
}

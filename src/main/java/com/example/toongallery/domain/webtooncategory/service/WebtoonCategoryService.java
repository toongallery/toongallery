package com.example.toongallery.domain.webtooncategory.service;

import com.example.toongallery.domain.category.entity.Category;
import com.example.toongallery.domain.category.repository.CategoryRepository;
import com.example.toongallery.domain.webtoon.entity.Webtoon;
import com.example.toongallery.domain.webtooncategory.entity.WebtoonCategory;
import com.example.toongallery.domain.webtooncategory.repository.WebtoonCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WebtoonCategoryService {
    private final WebtoonCategoryRepository webtoonCategoryRepository;
    private final CategoryRepository categoryRepository;

    @Transactional
    public void createWebtoonCategory(Webtoon webtoon, List<Category> categories){
        List<WebtoonCategory> webtoonCategoryList = categories.stream()
                .map(category -> new WebtoonCategory(category, webtoon))
                .collect(Collectors.toList());

        webtoonCategoryRepository.saveAll(webtoonCategoryList);
    }

    @Transactional(readOnly = true)
    public List<String> getCategoryNamesByWebtoonId(Long webtoonId){
        List<WebtoonCategory> webtoonCategories = webtoonCategoryRepository.findByWebtoonId(webtoonId);
        List<Long> categoryIds = webtoonCategories.stream()
                .map(WebtoonCategory::getCategoryId)
                .collect(Collectors.toList());

        return categoryRepository.findNamesById(categoryIds);
    }
}

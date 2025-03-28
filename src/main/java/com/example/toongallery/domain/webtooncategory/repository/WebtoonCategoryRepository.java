package com.example.toongallery.domain.webtooncategory.repository;

import com.example.toongallery.domain.category.entity.Category;
import com.example.toongallery.domain.webtooncategory.entity.WebtoonCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WebtoonCategoryRepository extends JpaRepository<WebtoonCategory, Long> {
    List<WebtoonCategory> findByWebtoonId(Long webtoonId);
}

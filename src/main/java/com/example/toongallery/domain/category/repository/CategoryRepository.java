package com.example.toongallery.domain.category.repository;

import com.example.toongallery.domain.category.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    boolean existsByCategoryName(String categoryName);
    List<Category> findByCategoryNameIn(Collection<String> categories);

    @Query("SELECT c.categoryName FROM Category c WHERE c.id IN :webtoonCategoryIds")
    List<String> findNamesById(@Param("webtoonCategoryIds")List<Long> webtoonCategoryIds);
}

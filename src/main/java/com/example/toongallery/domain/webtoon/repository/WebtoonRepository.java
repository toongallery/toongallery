package com.example.toongallery.domain.webtoon.repository;

import com.example.toongallery.domain.webtoon.entity.Webtoon;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface WebtoonRepository extends JpaRepository<Webtoon, Long>, WebtoonRepositoryCustom {
    Page<Webtoon> findAll(Pageable pageable);

    @Query("SELECT w FROM Webtoon w ORDER BY w.views DESC LIMIT 10")
    List<Webtoon> findPopularWebtoonsTop10();
}

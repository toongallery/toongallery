package com.example.toongallery.domain.webtoon.repository;

import com.example.toongallery.domain.webtoon.entity.Webtoon;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface WebtoonRepositoryCustom {
    Page<Webtoon> findBySearch(
            String keyword,
            List<String> genres,
            String authorName,
            Pageable pageable
    );
}

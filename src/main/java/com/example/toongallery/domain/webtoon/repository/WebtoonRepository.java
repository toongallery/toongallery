package com.example.toongallery.domain.webtoon.repository;

import com.example.toongallery.domain.webtoon.entity.Webtoon;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WebtoonRepository extends JpaRepository<Webtoon, Long> {
    Page<Webtoon> findAll(Pageable pageable);
}

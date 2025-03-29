package com.example.toongallery.domain.webtoon.repository;

import com.example.toongallery.domain.webtoon.entity.WebtoonViewLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface WebtoonViewLogRepository extends JpaRepository<WebtoonViewLog, Long> {
    boolean existsByUserIdAndWebtoonIdAndViewDate(Long userId, Long webtoonId, LocalDate viewDate);
}

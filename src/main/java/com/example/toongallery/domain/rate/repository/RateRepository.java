package com.example.toongallery.domain.rate.repository;

import com.example.toongallery.domain.rate.entity.Rate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RateRepository extends JpaRepository<Rate, Long> {
    // 특정 에피소드의 평균 별점 계산
    @Query("SELECT COALESCE(AVG(r.rates),0.0) FROM Rate r WHERE r.episode.id = :episodeId")
    Double findAverageRateByEpisodeId(@Param("episodeId") Long episodeId);

    @Query("SELECT AVG(r.rates) FROM Rate r WHERE r.episode.webtoon.id = :webtoonId")
    Double findAverageRateByWebtoonId(@Param("webtoonId") Long webtoonId);

    // 사용자가 특정 에피소드에 준 별점 조회
    @Query("SELECT r FROM Rate r WHERE r.user.id = :userId AND r.episode.id = :episodeId")
    Optional<Rate> findByUserIdAndEpisodeId(@Param("userId") Long userId, @Param("episodeId") Long episodeId);
}

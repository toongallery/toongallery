package com.example.toongallery.domain.episode.repository;

import com.example.toongallery.domain.episode.entity.Episode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EpisodeRepository extends JpaRepository<Episode, Long> {

    //에피소드 넘버로 정렬 리스트
    List<Episode> findByWebtoonIdOrderByEpisodeNumberAsc(Long webtoonId);

    // 회차 번호 계산
    @Query("SELECT MAX(e.episodeNumber) FROM Episode e WHERE e.webtoon.id = :webtoonId")
    Optional<Integer> findMaxEpisodeNumberByWebtoonId(@Param("webtoonId") Long webtoonId);

    // 에피소드id,웹툰id로 조회
    Optional<Episode> findByIdAndWebtoonId(Long episodeId, Long webtoonId);

    //제목 중복 검사
    boolean existsByWebtoonIdAndTitle(Long webtoonId, String title);

}

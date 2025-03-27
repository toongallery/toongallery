package com.example.toongallery.domain.episode.repository;

import com.example.toongallery.domain.episode.entity.Episode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EpisodeRepository extends JpaRepository<Episode, Long> {

    List<Episode> findByWebtoonIdOrderByEpisodeNumberAsc(Long webtoonId);

    @Query("SELECT MAX(e.episodeNumber) FROM Episode e WHERE e.webtoon.id = :webtoonId")
    Optional<Integer> findMaxEpisodeNumberByWebtoonId(@Param("webtoonId") Long webtoonId);

}

package com.example.toongallery.domain.episode.repository;

import com.example.toongallery.domain.episode.entity.Episode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EpisodeRepository extends JpaRepository<Episode, Long> {

    List<Episode> findByWebtoonIdOrderByEpisodeNumberAsc(Long webtoonId);
}

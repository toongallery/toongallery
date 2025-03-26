package com.example.toongallery.domain.episode.repository;

import com.example.toongallery.domain.episode.entity.Episode;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EpisodeRepository extends JpaRepository<Episode, Long> {

}

package com.example.toongallery.domain.image.repository;

import com.example.toongallery.domain.image.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ImageRepository extends JpaRepository<Image, Long> {
    List<Image> findByEpisodeIdOrderByImageIndexAsc(Long episodeId);
    List<Image> findImagesByEpisodeId(Long episodeId);
}

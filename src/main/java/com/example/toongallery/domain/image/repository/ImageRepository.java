package com.example.toongallery.domain.image.repository;

import com.example.toongallery.domain.image.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImageRepository extends JpaRepository<Image, Long> {
}

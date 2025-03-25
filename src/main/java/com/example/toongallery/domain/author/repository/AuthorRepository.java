package com.example.toongallery.domain.author.repository;

import com.example.toongallery.domain.author.entity.Author;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuthorRepository extends JpaRepository<Author, Long> {
    List<Author> findByUserId(Long userId);
    List<Author> findByWebtoonId(Long webtoonId);
}

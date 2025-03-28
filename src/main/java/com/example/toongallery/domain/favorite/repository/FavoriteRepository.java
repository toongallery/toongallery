package com.example.toongallery.domain.favorite.repository;

import com.example.toongallery.domain.favorite.entity.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    boolean existsByUserIdAndWebtoonId(Long userId, Long WebtoonId);

    void deleteByUserIdAndWebtoonId(Long userId, Long WebtoonId);

    int countByWebtoonId(Long WebtoonId);
}

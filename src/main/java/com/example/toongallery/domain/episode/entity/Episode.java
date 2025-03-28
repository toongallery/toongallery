package com.example.toongallery.domain.episode.entity;

import com.example.toongallery.domain.common.entity.BaseEntity;
import com.example.toongallery.domain.webtoon.entity.Webtoon;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "episodes")
public class Episode extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private Integer episodeNumber;

    private String thumbnailUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "webtoon_id", nullable = false)
    private Webtoon webtoon;

    public static Episode of(String title, Integer episodeNumber, String thumbnailUrl, Webtoon webtoon){
        Episode episode = new Episode();
        episode.title = title;
        episode.episodeNumber = episodeNumber;
        episode.thumbnailUrl = thumbnailUrl;
        episode.webtoon = webtoon;
        return episode;
    }

    public void updateThumbnail(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public void updateTitle(String title) {
        this.title = title;
    }

}

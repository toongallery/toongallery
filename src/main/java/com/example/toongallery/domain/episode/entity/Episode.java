package com.example.toongallery.domain.episode.entity;

import com.example.toongallery.domain.common.entity.BaseEntity;
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

// 내 코드 상엔 웹툰 엔티티가 없지만 만들 예정임
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "webtoon_id", nullable = false)
//    private Webtoon webtoon;

    public static Episode of(String title, Integer episodeNumber, String thumbnailUrl){
        Episode episode = new Episode();
        episode.title = title;
        episode.episodeNumber = episodeNumber;
        episode.thumbnailUrl = thumbnailUrl;
        return episode;
    }
}

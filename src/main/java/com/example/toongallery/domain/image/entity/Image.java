package com.example.toongallery.domain.image.entity;

import com.example.toongallery.domain.common.entity.BaseEntity;
import com.example.toongallery.domain.episode.entity.Episode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "images")
public class Image extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String imageName;

    private String imageUrl;

    private Integer imageIndex; //이미지 순서를 저장하는 필드 추가

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "episode_id", nullable = false)
    private Episode episode;

    public static Image of(String imageName, String imageUrl, Integer imageIndex, Episode episode){
        Image image = new Image();
        image.imageName = imageName;
        image.imageUrl = imageUrl;
        image.imageIndex = imageIndex;
        image.episode = episode;
        return image;
    }

}

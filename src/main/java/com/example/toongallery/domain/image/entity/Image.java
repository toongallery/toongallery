package com.example.toongallery.domain.image.entity;

import com.example.toongallery.domain.episode.entity.Episode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "images")
public class Image {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String imageName;

    private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "episode_id", nullable = false)
    private Episode episode;

    public static Image of(String imageName, String imageUrl){
        Image image = new Image();
        image.imageName = imageName;
        image.imageUrl = imageUrl;
        return image;
    }
}

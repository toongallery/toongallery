package com.example.toongallery.domain.webtoon.entity;

import com.example.toongallery.domain.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor
@Table(name = "webtoons")
public class Webtoon extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;//제목

    private String author;//작가

    private String genre;//장르

    private String thumbnail;//썸네일

    private String description;//설명

    private String day_of_week;//연재 요일

    private String status;//연재 상태

    private Double rate;//별점 평점

    private Integer favorite_count;//좋아요 수

    private Integer views;//조회수

    public Webtoon(String title, String author, String genre, String thumbnail, String description, String day_of_week, String status, Double rate, Integer favorite_count, Integer views) {
        this.title = title;
        this.author = author;
        this.genre = genre;
        this.thumbnail = thumbnail;
        this.description = description;
        this.day_of_week = day_of_week;
        this.status = status;
        this.rate = rate;
        this.favorite_count = favorite_count;
        this.views = views;
    }
}

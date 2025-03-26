package com.example.toongallery.domain.webtoon.entity;

import com.example.toongallery.domain.author.entity.Author;
import com.example.toongallery.domain.common.entity.BaseEntity;
import com.example.toongallery.domain.webtoon.enums.DayOfWeek;
import com.example.toongallery.domain.webtoon.enums.WebtoonStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@NoArgsConstructor
@Table(name = "webtoons")
public class Webtoon extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;//제목

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "webtoon_id")
    private List<Author> authors = new ArrayList<>();

    private String genre;//장르

    private String thumbnail;//썸네일

    private String description;//설명

    @Enumerated(EnumType.STRING)
    private DayOfWeek day_of_week;//연재 요일

    @Enumerated(EnumType.STRING)
    private WebtoonStatus status;//연재 상태

    private Double rate;//별점 평점

    private Integer favorite_count;//좋아요 수

    private Integer views;//조회수

    public Webtoon(String title, List<Author> authors ,String genre, String thumbnail, String description, DayOfWeek day_of_week, WebtoonStatus status) {
        this.title = title;
        this.authors = authors;
        this.genre = genre;
        this.thumbnail = thumbnail;
        this.description = description;
        this.day_of_week = day_of_week;
        this.status = status;
    }
}

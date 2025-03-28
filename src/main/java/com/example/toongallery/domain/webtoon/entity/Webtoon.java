package com.example.toongallery.domain.webtoon.entity;

import com.example.toongallery.domain.common.entity.BaseEntity;
import com.example.toongallery.domain.webtoon.enums.DayOfWeek;
import com.example.toongallery.domain.webtoon.enums.WebtoonStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "webtoons")
public class Webtoon extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;//제목

    private String thumbnail;//썸네일

    private String description;//설명

    @Enumerated(EnumType.STRING)
    private DayOfWeek day_of_week;//연재 요일

    @Enumerated(EnumType.STRING)
    private WebtoonStatus status;//연재 상태

    @ColumnDefault("0.0")
    private Double rate;//별점 평점

    @ColumnDefault("0")
    private Integer favorite_count;//좋아요 수

    @ColumnDefault("0")
    private Integer views = 0;//조회수

    public Webtoon(String title, String thumbnail, String description, DayOfWeek day_of_week, WebtoonStatus status) {
        this.title = title;
        this.thumbnail = thumbnail;
        this.description = description;
        this.day_of_week = day_of_week;
        this.status = status;
    }

    public static Webtoon of(String title, String thumbnail, String description,
                             DayOfWeek day_of_week, WebtoonStatus status){
        Webtoon webtoon = new Webtoon();
        webtoon.title = title;
        webtoon.thumbnail = thumbnail;
        webtoon.description = description;
        webtoon.day_of_week = day_of_week;
        webtoon.status = status;
        return webtoon;
    }

    public void updateThumbnail(String thumbnailUrl) {
        this.thumbnail = thumbnailUrl;
    }

    public void incrementView(){
        this.views = (this.views == null) ? 1:this.views + 1;
    }

    public void view(Integer cacheView){
        this.views = cacheView;
    }
}

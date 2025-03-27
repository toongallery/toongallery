package com.example.toongallery.domain.webtoon.dto.response;

import com.example.toongallery.domain.webtoon.enums.DayOfWeek;
import com.example.toongallery.domain.webtoon.enums.WebtoonStatus;
import lombok.Getter;

import java.util.List;

@Getter
public class WebtoonResponse {

    private final Long id;
    private final String title;
    private final List<String> authors;
    private final List<String> genres;
    private final String thumbnail;
    private final String description;
    private final DayOfWeek dayOfWeek;
    private final WebtoonStatus status;
    private final Double rate;
    private final Integer favoriteCount;
    private final Integer views;

    public WebtoonResponse(Long id, String title, List<String> authors,
                           List<String> genres, String thumbnail, String description,
                           DayOfWeek dayOfWeek, WebtoonStatus status, Double rate,
                           Integer favoriteCount, Integer views) {
        this.id = id;
        this.title = title;
        this.authors = authors;
        this.genres = genres;
        this.thumbnail = thumbnail;
        this.description = description;
        this.dayOfWeek = dayOfWeek;
        this.status = status;
        this.rate = rate != null ? rate : 0.0;
        this.favoriteCount = favoriteCount != null ? favoriteCount : 0;
        this.views = views != null ? views : 0;
    }
}

package com.example.toongallery.domain.webtoon.dto.response;

import com.example.toongallery.domain.author.entity.Author;
import com.example.toongallery.domain.webtoon.entity.Webtoon;
import com.example.toongallery.domain.webtoon.enums.DayOfWeek;
import com.example.toongallery.domain.webtoon.enums.WebtoonStatus;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

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

    public WebtoonResponse(Long id, String title, List<String> authors, List<String> genres, String thumbnail, String description, DayOfWeek dayOfWeek, WebtoonStatus status) {
        this.id = id;
        this.title = title;
        this.authors = authors;
        this.genres = genres;
        this.thumbnail = thumbnail;
        this.description = description;
        this.dayOfWeek = dayOfWeek;
        this.status = status;
    }
}

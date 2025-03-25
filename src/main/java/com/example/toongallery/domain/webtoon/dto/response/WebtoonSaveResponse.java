package com.example.toongallery.domain.webtoon.dto.response;

import com.example.toongallery.domain.webtoon.entity.Webtoon;
import com.example.toongallery.domain.webtoon.enums.DayOfWeek;
import com.example.toongallery.domain.webtoon.enums.WebtoonStatus;
import lombok.Getter;

import java.util.List;

@Getter
public class WebtoonSaveResponse {

    private final Long id;
    private final String title;
    private final List<String> authors;
    private final String genre;
    private final String thumbnail;
    private final String description;
    private final DayOfWeek dayOfWeek;
    private final WebtoonStatus status;

    public WebtoonSaveResponse(Webtoon webtoon, List<String> authors) {
        this.id = webtoon.getId();
        this.title = webtoon.getTitle();
        this.authors = authors;
        this.genre = webtoon.getGenre();
        this.thumbnail = webtoon.getThumbnail();
        this.description = webtoon.getDescription();
        this.dayOfWeek = webtoon.getDay_of_week();
        this.status = webtoon.getStatus();
    }
}

package com.example.toongallery.domain.webtoon.dto.response;

import com.example.toongallery.domain.webtoon.entity.Webtoon;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class WebtoonPopularResponse {
    private int rank;
    private String title;

    public WebtoonPopularResponse(int rank, Webtoon webtoon) {
        this.rank = rank;
        this.title = webtoon.getTitle();
    }
}

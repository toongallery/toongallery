package com.example.toongallery.domain.webtoon.dto.response;

import com.example.toongallery.domain.author.entity.Author;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class WebtoonPageResponse {
    private String title;
    private List<String> authors;
    private List<String> genres;
    private String thumbnail;
    private String description;
}

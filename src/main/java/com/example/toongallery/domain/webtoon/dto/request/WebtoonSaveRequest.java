package com.example.toongallery.domain.webtoon.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class WebtoonSaveRequest {

    @NotBlank
    private String title;
    @NotBlank
    private String author;
    @NotBlank
    private String genre;
    @NotBlank
    private String thumbnail;
    @NotBlank
    private String description;
    @NotBlank
    private String day_of_week;
    @NotBlank
    private String status;
}

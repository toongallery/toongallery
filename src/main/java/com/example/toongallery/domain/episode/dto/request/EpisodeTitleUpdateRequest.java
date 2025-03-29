package com.example.toongallery.domain.episode.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class EpisodeTitleUpdateRequest {
    @NotBlank
    private String title;
}

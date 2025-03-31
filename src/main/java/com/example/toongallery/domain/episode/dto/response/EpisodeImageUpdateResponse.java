package com.example.toongallery.domain.episode.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class EpisodeImageUpdateResponse {
    private List<String> imageUrls;
}
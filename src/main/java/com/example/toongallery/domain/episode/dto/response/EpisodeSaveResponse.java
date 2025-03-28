package com.example.toongallery.domain.episode.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class EpisodeSaveResponse {
    private Integer episodeNumber;
    private String title;
    private String thumbnailUrl;
    private List<String> imageUrls;
}

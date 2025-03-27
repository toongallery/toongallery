package com.example.toongallery.domain.episode.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class EpisodeResponseDto {
    private Long episodeId;
    private String title;
    private Integer episodeNumber;
    private String thumbnailUrl;
}

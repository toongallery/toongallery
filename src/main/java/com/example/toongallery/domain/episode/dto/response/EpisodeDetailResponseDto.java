package com.example.toongallery.domain.episode.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class EpisodeDetailResponseDto {
    private Long episodeId;
    private String title;
    private Integer episodeNumber;
    private List<String> imageUrls;
}

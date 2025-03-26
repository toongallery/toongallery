package com.example.toongallery.domain.episode.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class EpisodeSaveRequest {
    private String title;
    private Integer episodeNumber;
//    private MultipartFile thumbnail;
//    private List<MultipartFile> imageFiles;
}

package com.example.toongallery.domain.episode.controller;


import com.example.toongallery.domain.episode.dto.request.EpisodeSaveRequest;
import com.example.toongallery.domain.episode.entity.Episode;
import com.example.toongallery.domain.episode.service.EpisodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class EpisodeController {

    private final EpisodeService episodeService;

    @PostMapping("/webtoons/{webtoonId}/episodes")
    public ResponseEntity<String> createEpisode(
            @PathVariable Long webtoonId,
            @RequestPart(value = "json") EpisodeSaveRequest dto,
            @RequestPart(value ="thumbnail") MultipartFile thumbnailFile,
            @RequestPart(value ="images") List<MultipartFile> imageFiles

    ) throws Exception {
        Episode episode = episodeService.saveEpisode(webtoonId, dto, thumbnailFile, imageFiles);
        return ResponseEntity.ok("에피소드 등록 완료 (ID: " + episode.getId() + ")");
    }
}

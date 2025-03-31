package com.example.toongallery.domain.episode.controller;


import com.example.toongallery.domain.episode.dto.request.EpisodeSaveRequest;
import com.example.toongallery.domain.episode.dto.request.EpisodeTitleUpdateRequest;
import com.example.toongallery.domain.episode.dto.response.EpisodeDetailResponseDto;
import com.example.toongallery.domain.episode.dto.response.EpisodeImageUpdateResponse;
import com.example.toongallery.domain.episode.dto.response.EpisodeResponseDto;
import com.example.toongallery.domain.episode.dto.response.EpisodeSaveResponse;
import com.example.toongallery.domain.episode.entity.Episode;
import com.example.toongallery.domain.episode.service.EpisodeService;
import jakarta.validation.Valid;
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
    public ResponseEntity<EpisodeSaveResponse> createEpisode(
            @PathVariable Long webtoonId,
            @RequestPart(value = "json") EpisodeSaveRequest dto,
            @RequestPart(value ="thumbnail") MultipartFile thumbnailFile,
            @RequestPart(value ="images") List<MultipartFile> imageFiles

    ) {
        EpisodeSaveResponse response = episodeService.saveEpisode(webtoonId, dto, thumbnailFile, imageFiles);
        return ResponseEntity.ok(response);
    }
    @GetMapping("/webtoons/{webtoonId}/episodes")
    public ResponseEntity<List<EpisodeResponseDto>> getEpisodes(@PathVariable Long webtoonId) {
        List<EpisodeResponseDto> episodes = episodeService.getEpisodesByWebtoonId(webtoonId);
        return ResponseEntity.ok(episodes);
    }

    @GetMapping("/webtoons/{webtoonId}/episodes/{episodeId}")
    public ResponseEntity<EpisodeDetailResponseDto> getEpisodeDetail(@PathVariable Long episodeId) {
        EpisodeDetailResponseDto detail = episodeService.getEpisodeDetail(episodeId);
        return ResponseEntity.ok(detail);
    }

    @PatchMapping("/webtoons/{webtoonId}/episodes/{episodeId}/title")
    public ResponseEntity<Void> updateEpisodeTitle(
            @PathVariable Long webtoonId,
            @PathVariable Long episodeId,
            @Valid @RequestBody EpisodeTitleUpdateRequest request
    ) {
        episodeService.updateEpisodeTitle(webtoonId, episodeId, request.getTitle());
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/webtoons/{webtoonId}/episodes/{episodeId}/thumbnail")
    public ResponseEntity<String> updateEpisodeThumbnail(
            @PathVariable Long webtoonId,
            @PathVariable Long episodeId,
            @RequestPart("thumbnail") MultipartFile newThumbnailFile
    ) {
        String thumbnail = episodeService.updateEpisodeThumbnail(webtoonId, episodeId, newThumbnailFile);
        return ResponseEntity.ok(thumbnail);
    }

    @PatchMapping("/webtoons/{webtoonId}/episodes/{episodeId}/images")
    public ResponseEntity<EpisodeImageUpdateResponse> updateEpisodeImages(
            @PathVariable Long webtoonId,
            @PathVariable Long episodeId,
            @RequestPart("images") List<MultipartFile> newImageFiles
    ) {
        EpisodeImageUpdateResponse response = episodeService.updateEpisodeImages(webtoonId, episodeId, newImageFiles);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/webtoons/{webtoonId}/episodes/{episodeId}")
    public ResponseEntity<Void> deleteEpisode(
            @PathVariable Long webtoonId,
            @PathVariable Long episodeId
    ) {
        episodeService.deleteEpisode(webtoonId, episodeId);
        return ResponseEntity.noContent().build();
    }
}

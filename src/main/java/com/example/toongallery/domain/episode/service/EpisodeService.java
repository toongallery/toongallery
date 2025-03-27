package com.example.toongallery.domain.episode.service;

import com.example.toongallery.domain.common.exception.BaseException;
import com.example.toongallery.domain.common.exception.ErrorCode;
import com.example.toongallery.domain.episode.entity.Episode;
import com.example.toongallery.domain.episode.repository.EpisodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.example.toongallery.domain.episode.dto.request.EpisodeSaveRequest;
import com.example.toongallery.domain.episode.dto.response.EpisodeDetailResponseDto;
import com.example.toongallery.domain.episode.dto.response.EpisodeResponseDto;
import com.example.toongallery.domain.episode.entity.Episode;
import com.example.toongallery.domain.episode.repository.EpisodeRepository;
import com.example.toongallery.domain.image.entity.Image;
import com.example.toongallery.domain.image.repository.ImageRepository;
import com.example.toongallery.domain.image.service.ImageService;
import com.example.toongallery.domain.webtoon.entity.Webtoon;
import com.example.toongallery.domain.webtoon.repository.WebtoonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EpisodeService {
    private final EpisodeRepository episodeRepository;

    private final ImageRepository imageRepository;
    private final WebtoonRepository webtoonRepository;
    private final ImageService imageService;

    @Transactional
    public Episode saveEpisode(
            Long webtoonId,
            EpisodeSaveRequest dto,
            MultipartFile thumbnailFile,
            List<MultipartFile> imageFiles
    ) {
        // 1. 웹툰 조회
        Webtoon webtoon = webtoonRepository.findById(webtoonId)
                .orElseThrow(() -> new BaseException(ErrorCode.SERVER_NOT_WORK, null));

        // 2. 다음 회차 번호 계산
        int nextEpisodeNumber = episodeRepository.findMaxEpisodeNumberByWebtoonId(webtoonId)
                .orElse(0) + 1;

        // 3. 썸네일 업로드 → URL 반환 → 에피소드에 반영
        String thumbnailUrl = imageService.uploadEpisodeThumbnail(
                webtoonId,
                nextEpisodeNumber,
                thumbnailFile
        );

        // 4. 에피소드 저장 (썸네일은 null 상태로 우선 저장)
        Episode episode = Episode.of(dto.getTitle(), nextEpisodeNumber, thumbnailUrl, webtoon);
        episodeRepository.save(episode);

        // 5. 본문 이미지 업로드 및 저장
        imageService.uploadEpisodeImages(
                webtoonId,
                nextEpisodeNumber,
                imageFiles,
                episode
        );

        return episode;
    }

    @Transactional(readOnly = true)
    public List<EpisodeResponseDto> getEpisodesByWebtoonId(Long webtoonId) {
        List<Episode> episodes = episodeRepository.findByWebtoonIdOrderByEpisodeNumberAsc(webtoonId);

        return episodes.stream()
                .map(e -> new EpisodeResponseDto(
                        e.getId(),
                        e.getTitle(),
                        e.getEpisodeNumber(),
                        e.getThumbnailUrl()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public EpisodeDetailResponseDto getEpisodeDetail(Long episodeId) {
        Episode episode = episodeRepository.findById(episodeId)
                .orElseThrow(() -> new RuntimeException("에피소드가 존재하지 않습니다."));

        List<String> imageUrls = imageRepository.findByEpisodeIdOrderByImageIndexAsc(episodeId)
                .stream()
                .map(Image::getImageUrl)
                .toList();

        return new EpisodeDetailResponseDto(
                episode.getId(),
                episode.getTitle(),
                episode.getEpisodeNumber(),
                imageUrls
        );
    }

    public Episode getEpisode(Long episodeId) {
        return episodeRepository.findById(episodeId).orElseThrow(() ->
                new BaseException(ErrorCode.EPISODE_NOT_FOUND, null));
    }
}

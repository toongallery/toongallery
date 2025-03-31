package com.example.toongallery.domain.episode.service;

import com.example.toongallery.domain.comment.dto.response.CommentResponse;
import com.example.toongallery.domain.comment.repository.CommentRepository;
import com.example.toongallery.domain.common.exception.BaseException;
import com.example.toongallery.domain.common.exception.ErrorCode;
import com.example.toongallery.domain.episode.dto.request.EpisodeSaveRequest;
import com.example.toongallery.domain.episode.dto.response.EpisodeDetailResponseDto;
import com.example.toongallery.domain.episode.dto.response.EpisodeImageUpdateResponse;
import com.example.toongallery.domain.episode.dto.response.EpisodeResponseDto;
import com.example.toongallery.domain.episode.dto.response.EpisodeSaveResponse;
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

    private final CommentRepository commentRepository;

    @Transactional
    public EpisodeSaveResponse saveEpisode(
            Long webtoonId,
            EpisodeSaveRequest dto,
            MultipartFile thumbnailFile,
            List<MultipartFile> imageFiles
    ) {
        // 1. 웹툰 조회
        Webtoon webtoon = webtoonRepository.findById(webtoonId)
                .orElseThrow(() -> new BaseException(ErrorCode.WEBTOON_NOT_FOUND, null));

        // 2. 중복 제목 확인
        if (episodeRepository.existsByWebtoonIdAndTitle(webtoonId, dto.getTitle())) {
            throw new BaseException(ErrorCode.DUPLICATE_EPISODE_TITLE, "이미 존재하는 에피소드 제목입니다.");
        }

        // 3. 다음 회차 번호 계산
        int nextEpisodeNumber = episodeRepository.findMaxEpisodeNumberByWebtoonId(webtoonId)
                .orElse(0) + 1;

        // 4. 썸네일 업로드 → URL 반환 → 에피소드에 반영
        String thumbnailUrl = imageService.uploadEpisodeThumbnail(
                webtoonId,
                nextEpisodeNumber,
                thumbnailFile
        );

        // 5. 에피소드 저장 (썸네일은 null 상태로 우선 저장)
        Episode episode = Episode.of(dto.getTitle(), nextEpisodeNumber, thumbnailUrl, webtoon);
        episodeRepository.save(episode);

        // 6. 본문 이미지 업로드 및 저장
        List<Image> images = imageService.uploadEpisodeImages(
                webtoonId,
                nextEpisodeNumber,
                imageFiles,
                episode
        );
        List<String> imageUrls = images.stream()
                .map(Image::getImageUrl)
                .toList();

        return new EpisodeSaveResponse(
                episode.getEpisodeNumber(),
                episode.getTitle(),
                episode.getThumbnailUrl(),
                imageUrls
        );
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

        // 상위 10개의 베스트 댓글 출력
        List<CommentResponse> commentResponseList = commentRepository.findTop10CommentById(episodeId);


        return new EpisodeDetailResponseDto(
                episode.getId(),
                episode.getTitle(),
                episode.getEpisodeNumber(),
                imageUrls,
                commentResponseList
        );
    }

    public Episode getEpisode(Long episodeId) {
        return episodeRepository.findById(episodeId).orElseThrow(() ->
                new BaseException(ErrorCode.EPISODE_NOT_FOUND, null));
    }

    @Transactional
    public void updateEpisodeTitle(Long webtoonId, Long episodeId, String newTitle) {
        Episode episode = episodeRepository.findByIdAndWebtoonId(episodeId, webtoonId)
                .orElseThrow(() -> new BaseException(ErrorCode.EPISODE_NOT_FOUND, "회차 없음"));

        if (episodeRepository.existsByWebtoonIdAndTitle(webtoonId, newTitle)) {
            throw new BaseException(ErrorCode.DUPLICATE_EPISODE_TITLE, null);
        }


        episode.updateTitle(newTitle);
    }

    @Transactional
    public String updateEpisodeThumbnail(Long webtoonId, Long episodeId, MultipartFile newThumbnailFile) {
        // 1. 에피소드 조회
        Episode episode = episodeRepository.findByIdAndWebtoonId(episodeId, webtoonId)
                .orElseThrow(() -> new BaseException(ErrorCode.EPISODE_NOT_FOUND, "회차 없음"));

        int episodeNumber = episode.getEpisodeNumber();

        // 2. 기존 썸네일 삭제
        imageService.deleteEpisodeThumbnail(webtoonId, episodeNumber);

        // 3. 새 썸네일 업로드
        String newUrl = imageService.uploadEpisodeThumbnail(
                webtoonId,
                episodeNumber,
                newThumbnailFile
        );

        // 4. 에피소드에 반영
        episode.updateThumbnail(newUrl);
        return newUrl;
    }

    @Transactional
    public EpisodeImageUpdateResponse updateEpisodeImages(Long webtoonId, Long episodeId, List<MultipartFile> newImageFiles) {
        // 1. 에피소드 존재 확인
        Episode episode = episodeRepository.findByIdAndWebtoonId(episodeId, webtoonId)
                .orElseThrow(() -> new BaseException(ErrorCode.EPISODE_NOT_FOUND, "회차 없음"));

        // 2. 기존 이미지 삭제
        imageService.deleteEpisodeImages(episodeId);

        // 3. 웹툰 ID, 에피소드 번호로 새 이미지 업로드
        int episodeNumber = episode.getEpisodeNumber();

        List<Image> images = imageService.uploadEpisodeImages(webtoonId, episodeNumber, newImageFiles, episode);

        // 4. 업로드된 이미지 URL만 추출해서 응답
        List<String> imageUrls = images.stream()
                .map(Image::getImageUrl)
                .toList();

        return new EpisodeImageUpdateResponse(imageUrls);
    }

    public void deleteEpisode(Long webtoonId, Long episodeId) {
        // 1. 에피소드 존재 확인
        Episode episode = episodeRepository.findByIdAndWebtoonId(episodeId, webtoonId)
                .orElseThrow(() -> new BaseException(ErrorCode.EPISODE_NOT_FOUND, "회차 없음"));

        // 2. 연결된 이미지들 S3 + DB에서 삭제
        imageService.deleteEpisodeImages(episodeId);

        // 3. 에피소드 썸네일 S3에서 삭제
        imageService.deleteEpisodeThumbnail(webtoonId, episode.getEpisodeNumber());

        // 4. 에피소드 엔티티 삭제
        episodeRepository.delete(episode);
    }
    
}

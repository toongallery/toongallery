package com.example.toongallery.domain.episode.service;

import com.example.toongallery.domain.common.service.StorageService;
import com.example.toongallery.domain.common.util.FileUtils;
import com.example.toongallery.domain.episode.dto.request.EpisodeSaveRequest;
import com.example.toongallery.domain.episode.dto.response.EpisodeDetailResponseDto;
import com.example.toongallery.domain.episode.dto.response.EpisodeResponseDto;
import com.example.toongallery.domain.episode.entity.Episode;
import com.example.toongallery.domain.episode.repository.EpisodeRepository;
import com.example.toongallery.domain.image.entity.Image;
import com.example.toongallery.domain.image.repository.ImageRepository;
import com.example.toongallery.domain.webtoon.entity.Webtoon;
import com.example.toongallery.domain.webtoon.repository.WebtoonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EpisodeService {
    private final EpisodeRepository episodeRepository;
    private final ImageRepository imageRepository;
    private final WebtoonRepository webtoonRepository;
    private final StorageService storageService;

    @Transactional
    public Episode saveEpisode(Long webtoonId, EpisodeSaveRequest dto, MultipartFile thumbnailFile, List<MultipartFile> imageFiles)throws IOException{
        // 웹툰 조회
        Webtoon webtoon = webtoonRepository.findById(webtoonId)
                .orElseThrow(() -> new IllegalArgumentException("웹툰을 찾을 수 없습니다."));

        // 에피소드 저장 (썸네일은 일단 null)
        Episode episode = Episode.of(dto.getTitle(), dto.getEpisodeNumber(), null, webtoon);
        episodeRepository.save(episode);

        Long episodeId = episode.getId();

        // 썸네일 업로드
        String thumbPath = String.format("webtoons/%d/episodes/%d/thumbnail", webtoonId, episodeId);
        String thumbUrl = storageService.upload(thumbnailFile, thumbPath, "thumbnail.png");
        episode.updateThumbnail(thumbUrl);



        for (int i = 0; i < imageFiles.size(); i++) {
            MultipartFile file = imageFiles.get(i);
            String ext = FileUtils.getExtension(file.getOriginalFilename());
            String filename = String.format("%03d.%s", i, ext);

            String imagePath = String.format("webtoons/%d/episodes/%d/images", webtoonId, episodeId);
            String imageUrl = storageService.upload(file, imagePath, filename);

            Image image = Image.of(filename, imageUrl, i, episode);
            imageRepository.save(image);
        }

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
}

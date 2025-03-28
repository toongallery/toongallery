package com.example.toongallery.domain.episode;

import com.example.toongallery.domain.common.exception.BaseException;
import com.example.toongallery.domain.common.exception.ErrorCode;
import com.example.toongallery.domain.episode.entity.Episode;
import com.example.toongallery.domain.episode.dto.request.EpisodeSaveRequest;
import com.example.toongallery.domain.episode.dto.response.EpisodeDetailResponseDto;
import com.example.toongallery.domain.episode.dto.response.EpisodeResponseDto;
import com.example.toongallery.domain.episode.service.EpisodeService;
import com.example.toongallery.domain.image.entity.Image;
import com.example.toongallery.domain.image.repository.ImageRepository;
import com.example.toongallery.domain.image.service.ImageService;
import com.example.toongallery.domain.webtoon.entity.Webtoon;
import com.example.toongallery.domain.webtoon.repository.WebtoonRepository;
import com.example.toongallery.domain.episode.repository.EpisodeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import static org.junit.jupiter.api.Assertions.*;

class EpisodeServiceTest {

    @InjectMocks
    private EpisodeService episodeService;

    @Mock
    private EpisodeRepository episodeRepository;

    @Mock
    private WebtoonRepository webtoonRepository;

    @Mock
    private ImageRepository imageRepository;
    @Mock
    private ImageService imageService;

    @Mock
    private MultipartFile thumbnailFile;

    @Mock
    private List<MultipartFile> imageFiles;

    private Webtoon webtoon;
    private EpisodeSaveRequest episodeSaveRequest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Given
        webtoon = new Webtoon();
        webtoon.setId(1L);
        webtoon.setTitle("Test Webtoon");

        // Given
        episodeSaveRequest = new EpisodeSaveRequest("Test Episode");
    }

    @Test
    void 에피소드_저장_성공() {
        // Given
        Long webtoonId = 1L;
        int nextEpisodeNumber = 1;
        String thumbnailUrl = "http://thumbnail.url";

        when(webtoonRepository.findById(webtoonId)).thenReturn(Optional.of(webtoon));
        when(episodeRepository.findMaxEpisodeNumberByWebtoonId(webtoonId)).thenReturn(Optional.of(0));
        when(imageService.uploadEpisodeThumbnail(eq(webtoonId), eq(nextEpisodeNumber), eq(thumbnailFile))).thenReturn(thumbnailUrl);
        when(episodeRepository.save(any(Episode.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Episode episode = episodeService.saveEpisode(webtoonId, episodeSaveRequest, thumbnailFile, imageFiles);

        // Then
        assertNotNull(episode);
        assertEquals("Test Episode", episode.getTitle());
        assertEquals(nextEpisodeNumber, episode.getEpisodeNumber());
        assertEquals(thumbnailUrl, episode.getThumbnailUrl());
        verify(episodeRepository, times(1)).save(any(Episode.class));
        verify(imageService, times(1)).uploadEpisodeThumbnail(eq(webtoonId), eq(nextEpisodeNumber), eq(thumbnailFile));
    }

    @Test
    void 에피소드_조회() {
        // Given
        Long webtoonId = 1L;
        Episode episode1 = Episode.of("Test Episode 1", 1, "http://thumbnail1.url", webtoon);
        Episode episode2 = Episode.of("Test Episode 2", 2, "http://thumbnail2.url", webtoon);

        when(episodeRepository.findByWebtoonIdOrderByEpisodeNumberAsc(webtoonId))
                .thenReturn(Arrays.asList(episode1, episode2));

        // When
        List<EpisodeResponseDto> episodes = episodeService.getEpisodesByWebtoonId(webtoonId);

        // Then
        assertNotNull(episodes);
        assertEquals(2, episodes.size());
        assertEquals("Test Episode 1", episodes.get(0).getTitle());
        assertEquals("Test Episode 2", episodes.get(1).getTitle());
    }

    @Test
    void 에피소드_정보_조회() {
        // Given
        Long episodeId = 1L;
        Episode episode = Episode.of("Test Episode", 1, "http://thumbnail.url", webtoon);
        Image image = Image.of("Test Image", "http://image.url", 1, episode);

        when(episodeRepository.findById(episodeId)).thenReturn(Optional.of(episode));
        when(imageRepository.findByEpisodeIdOrderByImageIndexAsc(episodeId)).thenReturn(Arrays.asList(image));

        // When
        EpisodeDetailResponseDto episodeDetail = episodeService.getEpisodeDetail(episodeId);

        // Then
        assertNotNull(episodeDetail);
        assertEquals("Test Episode", episodeDetail.getTitle());
        assertEquals(1, episodeDetail.getEpisodeNumber());
        assertEquals(1, episodeDetail.getImageUrls().size());
        assertEquals("http://image.url", episodeDetail.getImageUrls().get(0));
    }

    @Test
    void 존재하지_않는_에피소드일때() {
        // Given
        Long episodeId = 999L;

        when(episodeRepository.findById(episodeId)).thenReturn(Optional.empty());

        // When & Then
        BaseException exception = assertThrows(BaseException.class, () -> episodeService.getEpisode(episodeId));
        assertEquals(ErrorCode.EPISODE_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void 에피소드_저장시_웹툰이_존재하지않음() {
        // Given
        Long webtoonId = 999L;

        when(webtoonRepository.findById(webtoonId)).thenReturn(Optional.empty());

        // When & Then
        BaseException exception = assertThrows(BaseException.class, () -> episodeService.saveEpisode(webtoonId, episodeSaveRequest, thumbnailFile, imageFiles));
        assertEquals(ErrorCode.SERVER_NOT_WORK, exception.getErrorCode());
    }

    @Test
    void 저장중_예외_발생() {
        // Given
        Long webtoonId = 1L;

        when(webtoonRepository.findById(webtoonId)).thenReturn(Optional.of(webtoon));
        when(episodeRepository.findMaxEpisodeNumberByWebtoonId(webtoonId)).thenReturn(Optional.of(0));
        when(imageService.uploadEpisodeThumbnail(eq(webtoonId), eq(1), eq(thumbnailFile))).thenReturn("http://thumbnail.url");
        when(episodeRepository.save(any(Episode.class))).thenThrow(new RuntimeException("Database Error"));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> episodeService.saveEpisode(webtoonId, episodeSaveRequest, thumbnailFile, imageFiles));
        assertEquals("Database Error", exception.getMessage());
    }
}

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

        // Given: 웹툰 데이터 준비
        webtoon = new Webtoon();
        webtoon.setId(1L);
        webtoon.setTitle("Test Webtoon");

        // Given: EpisodeSaveRequest 객체 준비
        episodeSaveRequest = new EpisodeSaveRequest("Test Episode");
    }

    @Test
    void testSaveEpisode() {
        // Given: 웹툰 ID, 썸네일 URL 준비
        Long webtoonId = 1L;
        int nextEpisodeNumber = 1;
        String thumbnailUrl = "http://thumbnail.url";

        // Mocking: 웹툰, 에피소드 저장과 관련된 서비스 메소드 설정
        when(webtoonRepository.findById(webtoonId)).thenReturn(Optional.of(webtoon));
        when(episodeRepository.findMaxEpisodeNumberByWebtoonId(webtoonId)).thenReturn(Optional.of(0));
        when(imageService.uploadEpisodeThumbnail(eq(webtoonId), eq(nextEpisodeNumber), eq(thumbnailFile))).thenReturn(thumbnailUrl);
        when(episodeRepository.save(any(Episode.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When: 에피소드 저장 메소드 호출
        Episode episode = episodeService.saveEpisode(webtoonId, episodeSaveRequest, thumbnailFile, imageFiles);

        // Then: 결과가 예상대로 나오는지 확인
        assertNotNull(episode);
        assertEquals("Test Episode", episode.getTitle());
        assertEquals(nextEpisodeNumber, episode.getEpisodeNumber());
        assertEquals(thumbnailUrl, episode.getThumbnailUrl());
        verify(episodeRepository, times(1)).save(any(Episode.class));
        verify(imageService, times(1)).uploadEpisodeThumbnail(eq(webtoonId), eq(nextEpisodeNumber), eq(thumbnailFile));
    }

    @Test
    void testGetEpisodesByWebtoonId() {
        // Given: 웹툰 ID와 두 개의 에피소드 준비
        Long webtoonId = 1L;
        Episode episode1 = Episode.of("Test Episode 1", 1, "http://thumbnail1.url", webtoon);
        Episode episode2 = Episode.of("Test Episode 2", 2, "http://thumbnail2.url", webtoon);

        // Mocking: 에피소드 조회 메소드 설정
        when(episodeRepository.findByWebtoonIdOrderByEpisodeNumberAsc(webtoonId))
                .thenReturn(Arrays.asList(episode1, episode2));

        // When: 웹툰 ID로 에피소드 조회
        List<EpisodeResponseDto> episodes = episodeService.getEpisodesByWebtoonId(webtoonId);

        // Then: 에피소드 리스트의 크기와 값이 예상대로 나오는지 확인
        assertNotNull(episodes);
        assertEquals(2, episodes.size());
        assertEquals("Test Episode 1", episodes.get(0).getTitle());
        assertEquals("Test Episode 2", episodes.get(1).getTitle());
    }

    @Test
    void testGetEpisodeDetail() {
        // Given: 에피소드 ID와 해당 에피소드에 속하는 이미지 준비
        Long episodeId = 1L;
        Episode episode = Episode.of("Test Episode", 1, "http://thumbnail.url", webtoon);
        Image image = Image.of("Test Image", "http://image.url", 1, episode);

        // Mocking: 에피소드 조회와 이미지 조회 메소드 설정
        when(episodeRepository.findById(episodeId)).thenReturn(Optional.of(episode));
        when(imageRepository.findByEpisodeIdOrderByImageIndexAsc(episodeId)).thenReturn(Arrays.asList(image));

        // When: 에피소드 상세 조회
        EpisodeDetailResponseDto episodeDetail = episodeService.getEpisodeDetail(episodeId);

        // Then: 에피소드 상세 정보가 예상대로 나오는지 확인
        assertNotNull(episodeDetail);
        assertEquals("Test Episode", episodeDetail.getTitle());
        assertEquals(1, episodeDetail.getEpisodeNumber());
        assertEquals(1, episodeDetail.getImageUrls().size());
        assertEquals("http://image.url", episodeDetail.getImageUrls().get(0));
    }

    @Test
    void testGetEpisodeNotFound() {
        // Given: 존재하지 않는 에피소드 ID
        Long episodeId = 999L;

        // Mocking: 에피소드 조회가 비어 있는 경우
        when(episodeRepository.findById(episodeId)).thenReturn(Optional.empty());

        // When & Then: 에피소드가 없을 때 예외가 발생하는지 확인
        BaseException exception = assertThrows(BaseException.class, () -> episodeService.getEpisode(episodeId));
        assertEquals(ErrorCode.EPISODE_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void testSaveEpisode_WebtoonNotFound() {
        // Given: 잘못된 웹툰 ID
        Long webtoonId = 999L;

        // Mocking: 웹툰 조회 시 예외를 던지도록 설정
        when(webtoonRepository.findById(webtoonId)).thenReturn(Optional.empty());

        // When & Then: 웹툰이 존재하지 않으면 예외가 발생하는지 확인
        BaseException exception = assertThrows(BaseException.class, () -> episodeService.saveEpisode(webtoonId, episodeSaveRequest, thumbnailFile, imageFiles));
        assertEquals(ErrorCode.SERVER_NOT_WORK, exception.getErrorCode());
    }

    @Test
    void testSaveEpisode_SaveError() {
        // Given: 정상적인 웹툰 ID와 에피소드 저장 요청
        Long webtoonId = 1L;

        // Mocking: 에피소드 저장 시 예외 발생하도록 설정
        when(webtoonRepository.findById(webtoonId)).thenReturn(Optional.of(webtoon));
        when(episodeRepository.findMaxEpisodeNumberByWebtoonId(webtoonId)).thenReturn(Optional.of(0));
        when(imageService.uploadEpisodeThumbnail(eq(webtoonId), eq(1), eq(thumbnailFile))).thenReturn("http://thumbnail.url");
        when(episodeRepository.save(any(Episode.class))).thenThrow(new RuntimeException("Database Error"));

        // When & Then: 에피소드 저장 시 예외가 발생하는지 확인
        RuntimeException exception = assertThrows(RuntimeException.class, () -> episodeService.saveEpisode(webtoonId, episodeSaveRequest, thumbnailFile, imageFiles));
        assertEquals("Database Error", exception.getMessage());
    }
}

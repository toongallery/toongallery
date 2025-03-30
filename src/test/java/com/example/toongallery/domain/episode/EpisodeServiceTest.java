package com.example.toongallery.domain.episode;

import com.example.toongallery.domain.comment.dto.response.CommentResponse;
import com.example.toongallery.domain.comment.entity.Comment;
import com.example.toongallery.domain.comment.repository.CommentRepository;
import com.example.toongallery.domain.common.exception.BaseException;
import com.example.toongallery.domain.common.exception.ErrorCode;
import com.example.toongallery.domain.episode.dto.response.EpisodeImageUpdateResponse;
import com.example.toongallery.domain.episode.entity.Episode;
import com.example.toongallery.domain.episode.dto.request.EpisodeSaveRequest;
import com.example.toongallery.domain.episode.dto.response.EpisodeDetailResponseDto;
import com.example.toongallery.domain.episode.dto.response.EpisodeResponseDto;
import com.example.toongallery.domain.episode.dto.response.EpisodeSaveResponse;
import com.example.toongallery.domain.episode.service.EpisodeService;
import com.example.toongallery.domain.image.entity.Image;
import com.example.toongallery.domain.image.repository.ImageRepository;
import com.example.toongallery.domain.image.service.ImageService;
import com.example.toongallery.domain.user.entity.User;
import com.example.toongallery.domain.webtoon.entity.Webtoon;
import com.example.toongallery.domain.webtoon.repository.WebtoonRepository;
import com.example.toongallery.domain.episode.repository.EpisodeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;

import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
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

    @Mock
    private CommentRepository commentRepository;
    @Mock
    private Webtoon webtoon;
    @Mock
    private EpisodeSaveRequest episodeSaveRequest;

    @Mock
    private Comment comment;

    @Mock
    private Episode episode;

    @BeforeEach
    void setUp() {

        episode = Episode.of("Test Episode", 1, "test_thumbnail.jpg", null);
        ReflectionTestUtils.setField(episode, "id", 1L);
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
        EpisodeSaveResponse response = episodeService.saveEpisode(webtoonId, episodeSaveRequest, thumbnailFile, imageFiles);

        // Then
        assertNotNull(response);
        assertEquals("Test Episode", response.getTitle());
        assertEquals(nextEpisodeNumber, response.getEpisodeNumber());
        assertEquals(thumbnailUrl, response.getThumbnailUrl());
        assertNotNull(response.getImageUrls());
        verify(episodeRepository, times(1)).save(any(Episode.class));
        verify(imageService, times(1)).uploadEpisodeThumbnail(eq(webtoonId), eq(nextEpisodeNumber), eq(thumbnailFile));
    }

    @Test
    void 에피소드_저장_중_중복된_제목_에러() {
        // Given
        Long webtoonId = 1L;
        String duplicateTitle = "중복된 제목";
        EpisodeSaveRequest dto = new EpisodeSaveRequest(duplicateTitle);
        MultipartFile thumbnailFile = mock(MultipartFile.class);
        List<MultipartFile> imageFiles = mock(List.class);

        Webtoon webtoon = new Webtoon();
        webtoon.setTitle("테스트 웹툰");

        when(webtoonRepository.findById(webtoonId)).thenReturn(Optional.of(webtoon));
        when(episodeRepository.existsByWebtoonIdAndTitle(webtoonId, duplicateTitle)).thenReturn(true);

        // When & Then
        BaseException exception = assertThrows(BaseException.class, () -> episodeService.saveEpisode(webtoonId, dto, thumbnailFile, imageFiles));
        assertEquals(ErrorCode.DUPLICATE_EPISODE_TITLE, exception.getErrorCode()); // 에러 코드 확인
    }

    @Test
    void 에피소드_썸네일_업데이트() {
        // Given
        Long webtoonId = 1L;
        Long episodeId = 1L;
        String newThumbnailUrl = "http://newthumbnail.url";
        MultipartFile newThumbnailFile = mock(MultipartFile.class);

        Episode episode = Episode.of("Test Episode", 1, "http://oldthumbnail.url", webtoon);

        when(episodeRepository.findByIdAndWebtoonId(episodeId, webtoonId)).thenReturn(Optional.of(episode));
        when(imageService.uploadEpisodeThumbnail(eq(webtoonId), eq(1), eq(newThumbnailFile))).thenReturn(newThumbnailUrl);

        // When
        String updatedThumbnailUrl = episodeService.updateEpisodeThumbnail(webtoonId, episodeId, newThumbnailFile);

        // Then
        assertEquals(newThumbnailUrl, updatedThumbnailUrl);
        verify(imageService, times(1)).deleteEpisodeThumbnail(eq(webtoonId), eq(1)); // 이전 썸네일 삭제
        verify(imageService, times(1)).uploadEpisodeThumbnail(eq(webtoonId), eq(1), eq(newThumbnailFile)); // 새 썸네일 업로드
    }

    @Test
    void 에피소드_이미지_업데이트() {
        // Given
        Long webtoonId = 1L;
        Long episodeId = 1L;
        List<MultipartFile> newImageFiles = mock(List.class);

        Episode episode = Episode.of("Test Episode", 1, "http://thumbnail.url", webtoon);
        List<Image> images = Arrays.asList(
                Image.of("image1", "http://image1.url", 0, episode),
                Image.of("image2", "http://image2.url", 1, episode)
        );

        // 에피소드 존재 확인
        when(episodeRepository.findByIdAndWebtoonId(episodeId, webtoonId)).thenReturn(Optional.of(episode));

        // 새로운 이미지 업로드 Mocking (여기서 빈 리스트가 반환되면 안됨)
        when(imageService.uploadEpisodeImages(eq(webtoonId), eq(1), eq(newImageFiles), eq(episode)))
                .thenReturn(images);

        // When
        EpisodeImageUpdateResponse response = episodeService.updateEpisodeImages(webtoonId, episodeId, newImageFiles);

        // Then
        assertNotNull(response);
        assertNotNull(response.getImageUrls());
        assertEquals(2, response.getImageUrls().size());

        verify(imageService, times(1)).deleteEpisodeImages(eq(episodeId)); // 기존 이미지 삭제 확인
        verify(imageService, times(1)).uploadEpisodeImages(eq(webtoonId), eq(1), eq(newImageFiles), eq(episode)); // 새로운 이미지 업로드 확인
    }


    @Test
    void 에피소드_상세_정보_조회_성공() {
        // Given
        Long episodeid = 1L;

        User user = new User();

        // 댓글과 관련된 설정 (episode를 반드시 설정)
        Comment comment1 = new Comment(episode, user, "Great episode!", null); // episode를 연결
        Comment comment2 = new Comment(episode, user, "Loved it!", null); // episode를 연결

        // CommentResponse로 변환된 객체들
        CommentResponse commentResponse1 = CommentResponse.of(comment1);
        CommentResponse commentResponse2 = CommentResponse.of(comment2);

        // 댓글 리스트 생성
        List<CommentResponse> commentResponses = Arrays.asList(commentResponse1, commentResponse2);

        // Mocking episodeRepository.findById(episodeId)와 commentRepository.findTop10CommentById(episodeId)
        when(episodeRepository.findById(episodeid)).thenReturn(Optional.of(episode));  // 추가된 부분
        when(commentRepository.findTop10CommentById(episodeid)).thenReturn(commentResponses);

        // When
        EpisodeDetailResponseDto response = episodeService.getEpisodeDetail(episodeid);

        // Then
        assertNotNull(response);
        assertEquals(episodeid, response.getEpisodeId());
        assertEquals("Test Episode", response.getTitle());
        assertEquals(1, response.getEpisodeNumber());
        assertEquals(2, response.getTopTenComments().size());
        assertEquals("Great episode!", response.getTopTenComments().get(0).getContent());
        assertEquals("Loved it!", response.getTopTenComments().get(1).getContent());

        verify(episodeRepository, times(1)).findById(episodeid);
        verify(commentRepository, times(1)).findTop10CommentById(episodeid);
    }


    @Test
    void 에피소드_상세_정보_조회_시_에피소드_존재하지_않을_경우_에러() {
        // Given
        Long invalidEpisodeId = 999L;

        when(episodeRepository.findById(invalidEpisodeId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> episodeService.getEpisodeDetail(invalidEpisodeId));
    }

    @Test
    void 에피소드_조회_성공() {
        // Given
        Long episodeId = 1L;
        Episode episode = Episode.of("기존 제목", 1, "http://thumbnail.url", new Webtoon());

        when(episodeRepository.findById(episodeId)).thenReturn(Optional.of(episode));

        // When
        Episode response = episodeService.getEpisode(episodeId);
        ReflectionTestUtils.setField(response, "id", 1L);
        // Then
        assertNotNull(response);
        assertEquals(episodeId, response.getId());
        assertEquals("기존 제목", response.getTitle()); // 올바른 제목 비교

        verify(episodeRepository, times(1)).findById(episodeId);
    }


    @Test
    void 에피소드_조회_시_에피소드_존재하지_않을_경우_에러() {
        // Given
        Long invalidEpisodeId = 999L;

        when(episodeRepository.findById(invalidEpisodeId)).thenReturn(Optional.empty());

        // When & Then
        BaseException exception = assertThrows(BaseException.class, () -> episodeService.getEpisode(invalidEpisodeId));
        assertEquals(ErrorCode.EPISODE_NOT_FOUND, exception.getErrorCode()); // 에러 코드 확인
    }


    @Test
    void 에피소드_제목_수정_성공() {
        // Given
        Long webtoonId = 1L;
        Long episodeId = 1L;
        String newTitle = "Updated Episode Title";
        Episode episode = Episode.of("기존 제목", 1, "http://thumbnail.url", new Webtoon());

        when(episodeRepository.findByIdAndWebtoonId(episodeId, webtoonId)).thenReturn(Optional.of(episode));
        when(episodeRepository.existsByWebtoonIdAndTitle(webtoonId, newTitle)).thenReturn(false);

        // When
        episodeService.updateEpisodeTitle(webtoonId, episodeId, newTitle);

        // Then
        assertEquals(newTitle, episode.getTitle());
        verify(episodeRepository, times(1)).findByIdAndWebtoonId(episodeId, webtoonId);
        verify(episodeRepository, times(1)).existsByWebtoonIdAndTitle(webtoonId, newTitle);
    }

    @Test
    void 에피소드_제목_수정_중_중복된_제목_에러() {
        // Given
        Long webtoonId = 1L;
        Long episodeId = 1L;
        String newTitle = "중복된 제목";
        Episode existingEpisode = Episode.of("기존 제목", 1, "http://thumbnail.url", new Webtoon());

        when(episodeRepository.findByIdAndWebtoonId(episodeId, webtoonId)).thenReturn(Optional.of(existingEpisode));
        when(episodeRepository.existsByWebtoonIdAndTitle(webtoonId, newTitle)).thenReturn(true);

        // When & Then
        BaseException exception = assertThrows(BaseException.class, () -> episodeService.updateEpisodeTitle(webtoonId, episodeId, newTitle));
        assertEquals(ErrorCode.DUPLICATE_EPISODE_TITLE, exception.getErrorCode()); // 에러 코드 확인
    }

    @Test
    void 에피소드_상세정보_조회_시_에피소드_존재하지_않을_경우_에러() {
        // Given
        Long invalidEpisodeId = 999L;

        when(episodeRepository.findById(invalidEpisodeId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> episodeService.getEpisodeDetail(invalidEpisodeId));
    }

    @Test
    void 에피소드_삭제_이미지_실패() {
        // Given
        Long episodeId = 999L;

        // When & Then
        BaseException exception = assertThrows(BaseException.class, () -> episodeService.updateEpisodeImages(1L, episodeId, imageFiles));
        assertEquals(ErrorCode.EPISODE_NOT_FOUND, exception.getErrorCode()); // 에러 코드 확인
    }
}

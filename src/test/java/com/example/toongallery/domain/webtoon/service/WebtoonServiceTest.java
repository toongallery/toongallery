package com.example.toongallery.domain.webtoon.service;

import com.example.toongallery.domain.author.service.AuthorService;
import com.example.toongallery.domain.common.dto.AuthUser;
import com.example.toongallery.domain.common.exception.BaseException;
import com.example.toongallery.domain.common.exception.ErrorCode;
import com.example.toongallery.domain.user.entity.User;
import com.example.toongallery.domain.user.enums.Gender;
import com.example.toongallery.domain.user.enums.UserRole;
import com.example.toongallery.domain.user.repository.UserRepository;
import com.example.toongallery.domain.webtoon.dto.request.WebtoonSaveRequest;
import com.example.toongallery.domain.webtoon.dto.response.WebtoonResponse;
import com.example.toongallery.domain.webtoon.entity.Webtoon;
import com.example.toongallery.domain.webtoon.enums.DayOfWeek;
import com.example.toongallery.domain.webtoon.enums.WebtoonStatus;
import com.example.toongallery.domain.webtoon.repository.WebtoonRepository;
import com.example.toongallery.domain.webtooncategory.service.WebtoonCategoryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class WebtoonServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private WebtoonRepository webtoonRepository;
    @Mock private AuthorService authorService;
    @Mock private WebtoonCategoryService webtoonCategoryService;
    @InjectMocks private WebtoonService webtoonService;

    @DisplayName("웹툰 저장 실패 - 로그인한 유저가 작가가 아님")
    @Test
    void saveWebtoon_실패_작성자가아님() {
        // given
        AuthUser authUser = new AuthUser(1L, "user@example.com", UserRole.ROLE_USER); // 작가 아님
        WebtoonSaveRequest request = new WebtoonSaveRequest(
                "웹툰 제목",
                List.of("작가1"),
                List.of("로맨스"),
                "설명입니다",
                DayOfWeek.MON
        );
        MultipartFile thumbnailFile = mock(MultipartFile.class);

        User user = new User(
                "user@example.com", "pw", "유저", LocalDate.now(), Gender.MALE, UserRole.ROLE_USER
        );
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));

        // when & then
        BaseException ex = assertThrows(BaseException.class, () ->
                webtoonService.saveWebtoon(authUser, request, thumbnailFile));

        assertEquals(ErrorCode.INVALID_USER_ROLE, ex.getErrorCode());
        assertEquals("작가만 웹툰 등록 가능", ex.getField());
    }

    @DisplayName("웹툰 저장 실패 - 작가 목록에 존재하지 않는 유저 포함")
    @Test
    void saveWebtoon_실패_존재하지않는작가() {
        // given
        AuthUser authUser = new AuthUser(1L, "author@example.com", UserRole.ROLE_AUTHOR);
        WebtoonSaveRequest request = new WebtoonSaveRequest(
                "웹툰 제목",
                List.of("작가1", "작가2"),
                List.of("로맨스"),
                "설명입니다",
                DayOfWeek.MON
        );
        MultipartFile thumbnailFile = mock(MultipartFile.class);

        User authorUser = new User(
                "author@example.com", "pw", "작가1", LocalDate.now(), Gender.MALE, UserRole.ROLE_AUTHOR
        );

        when(userRepository.findByEmail("author@example.com")).thenReturn(Optional.of(authorUser));
        when(userRepository.findByNameIn(List.of("작가1", "작가2")))
                .thenReturn(List.of(authorUser)); // 작가2 없음

        // when & then
        BaseException ex = assertThrows(BaseException.class, () ->
                webtoonService.saveWebtoon(authUser, request, thumbnailFile));

        assertEquals(ErrorCode.USER_NOT_FOUND, ex.getErrorCode());
        assertTrue(ex.getField().contains("작가2")); // 누락된 작가 이름 포함 여부
    }


    @Test
    @DisplayName("전체 웹툰 조회 - 작가, 카테고리 포함")
    void getWebtoons_성공() {
        // given
        Webtoon webtoon1 = Webtoon.of("웹툰1", "thumb1.jpg", "설명1", DayOfWeek.MON, WebtoonStatus.ONGOING);
        ReflectionTestUtils.setField(webtoon1, "id", 1L);

        Webtoon webtoon2 = Webtoon.of("웹툰2", "thumb2.jpg", "설명2", DayOfWeek.TUE, WebtoonStatus.ONGOING);
        ReflectionTestUtils.setField(webtoon2, "id", 2L);

        Page<Webtoon> mockPage = new PageImpl<>(List.of(webtoon1, webtoon2));
        when(webtoonRepository.findAll(any(Pageable.class))).thenReturn(mockPage);

        when(authorService.getAuthorNamesByWebtoonId(1L)).thenReturn(List.of("작가A"));
        when(authorService.getAuthorNamesByWebtoonId(2L)).thenReturn(List.of("작가B", "작가C"));

        when(webtoonCategoryService.getCategoryNamesByWebtoonId(1L)).thenReturn(List.of("로맨스"));
        when(webtoonCategoryService.getCategoryNamesByWebtoonId(2L)).thenReturn(List.of("액션", "판타지"));

        // when
        Page<WebtoonResponse> result = webtoonService.getWebtoons(1, 10);

        // then
        assertEquals(2, result.getContent().size());

        WebtoonResponse w1 = result.getContent().get(0);
        assertEquals("웹툰1", w1.getTitle());
        assertEquals(List.of("작가A"), w1.getAuthors());
        assertEquals(List.of("로맨스"), w1.getGenres());

        WebtoonResponse w2 = result.getContent().get(1);
        assertEquals("웹툰2", w2.getTitle());
        assertEquals(List.of("작가B", "작가C"), w2.getAuthors());
        assertEquals(List.of("액션", "판타지"), w2.getGenres());
    }

    @DisplayName("전체 웹툰 조회 - 결과 없음 (빈 페이지)")
    @Test
    void getWebtoons_빈결과() {
        // given
        Page<Webtoon> emptyPage = new PageImpl<>(List.of());
        when(webtoonRepository.findAll(any(Pageable.class))).thenReturn(emptyPage);

        // when
        Page<WebtoonResponse> result = webtoonService.getWebtoons(1, 10);

        // then
        assertNotNull(result);
        assertEquals(0, result.getContent().size());
    }


}

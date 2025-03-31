package com.example.toongallery.domain.favorite;

import com.example.toongallery.domain.common.exception.BaseException;
import com.example.toongallery.domain.common.exception.ErrorCode;
import com.example.toongallery.domain.favorite.entity.Favorite;
import com.example.toongallery.domain.favorite.repository.FavoriteRepository;
import com.example.toongallery.domain.favorite.service.FavoriteService;
import com.example.toongallery.domain.user.entity.User;
import com.example.toongallery.domain.user.enums.Gender;
import com.example.toongallery.domain.user.enums.UserRole;
import com.example.toongallery.domain.user.repository.UserRepository;
import com.example.toongallery.domain.webtoon.entity.Webtoon;
import com.example.toongallery.domain.webtoon.repository.WebtoonRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FavoriteServiceTest {

    @InjectMocks
    private FavoriteService favoriteService;

    @Mock
    private FavoriteRepository favoriteRepository;

    @Mock
    private WebtoonRepository webtoonRepository;

    @Mock
    private UserRepository userRepository;

    private User user;
    private Webtoon webtoon;

    @BeforeEach
    public void setUp() {
        user = new User("test@example.com", "password", "Test User", LocalDate.of(1990, 1, 1), Gender.MALE, UserRole.ROLE_USER);

        webtoon = new Webtoon();
        webtoon.setId(1L);
    }

    @Test
    @DisplayName("이미 즐겨찾기 상태일 경우 삭제 후 false 반환")
    void shouldRemoveFavoriteIfAlreadyExists() {
        // Given
        given(favoriteRepository.existsByUserIdAndWebtoonId(user.getId(), webtoon.getId()))
                .willReturn(true);
        willDoNothing().given(favoriteRepository).deleteByUserIdAndWebtoonId(user.getId(), webtoon.getId());

        // When
        boolean result = favoriteService.toggle(user.getId(), webtoon.getId());

        // Then
        assertThat(result).isFalse(); // 좋아요가 취소되어야 함
        verify(favoriteRepository).deleteByUserIdAndWebtoonId(user.getId(), webtoon.getId());
    }

    @Test
    @DisplayName("즐겨찾기가 없을 경우 추가 후 true 반환")
    void shouldAddFavoriteIfNotExists() {
        // Given
        given(favoriteRepository.existsByUserIdAndWebtoonId(user.getId(), webtoon.getId()))
                .willReturn(false);
        given(webtoonRepository.findById(webtoon.getId()))
                .willReturn(Optional.of(webtoon));
        given(userRepository.findById(user.getId()))
                .willReturn(Optional.of(user));
        given(favoriteRepository.save(any(Favorite.class)))
                .willReturn(Favorite.builder().user(user).webtoon(webtoon).build());

        // When
        boolean result = favoriteService.toggle(user.getId(), webtoon.getId());

        // Then
        assertThat(result).isTrue(); // 즐겨찾기 추가 성공
        verify(favoriteRepository).save(any(Favorite.class));
    }

    @Test
    @DisplayName("웹툰이 존재하지 않을 경우 예외 발생")
    void shouldThrowExceptionIfWebtoonNotExists() {
        // Given
        given(favoriteRepository.existsByUserIdAndWebtoonId(user.getId(), webtoon.getId()))
                .willReturn(false);
        given(webtoonRepository.findById(webtoon.getId()))
                .willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> favoriteService.toggle(user.getId(), webtoon.getId()))
                .isInstanceOf(BaseException.class)
                .hasMessage(ErrorCode.COMMENT_NOT_EXIST.getMessage());
    }

    @Test
    @DisplayName("유저가 존재하지 않을 경우 예외 발생")
    void shouldThrowExceptionIfUserNotExists() {
        // Given
        given(favoriteRepository.existsByUserIdAndWebtoonId(user.getId(), webtoon.getId()))
                .willReturn(false);
        given(webtoonRepository.findById(webtoon.getId()))
                .willReturn(Optional.of(webtoon));
        given(userRepository.findById(user.getId()))
                .willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> favoriteService.toggle(user.getId(), webtoon.getId()))
                .isInstanceOf(BaseException.class)
                .hasMessage(ErrorCode.USER_NOT_EXIST.getMessage());
    }
}

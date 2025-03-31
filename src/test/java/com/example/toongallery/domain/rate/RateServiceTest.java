package com.example.toongallery.domain.rate;

import com.example.toongallery.domain.common.exception.BaseException;
import com.example.toongallery.domain.common.exception.ErrorCode;
import com.example.toongallery.domain.episode.entity.Episode;
import com.example.toongallery.domain.episode.repository.EpisodeRepository;
import com.example.toongallery.domain.rate.entity.Rate;
import com.example.toongallery.domain.rate.repository.RateRepository;
import com.example.toongallery.domain.rate.service.RateService;
import com.example.toongallery.domain.user.entity.User;
import com.example.toongallery.domain.user.repository.UserRepository;
import com.example.toongallery.domain.webtoon.entity.Webtoon;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.OptimisticLockingFailureException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RateServiceTest {

    @InjectMocks
    private RateService rateService;

    @Mock
    private RateRepository rateRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EpisodeRepository episodeRepository;

    private User user1;
    private User user2;
    private Episode episode;
    private Webtoon webtoon;


    @BeforeEach
    public void setUp() {
        user1 = new User("user1@example.com", "password", "User One", null, null, null);
        user1.setId(1L);
        user2 = new User("user2@example.com", "password", "User Two", null, null, null);
        user2.setId(2L);

        webtoon = new Webtoon();
        webtoon.setId(1L);

        episode = Episode.of("Episode 1", 1, "thumbnail_url", webtoon);
        episode.setId(1L);
    }

    @Test
    void rating이_없으면_새로_저장() {
        // Given

        given(rateRepository.findByUserIdAndEpisodeId(user1.getId(), episode.getId()))
                .willReturn(Optional.empty());
        given(episodeRepository.findById(episode.getId()))
                .willReturn(Optional.of(episode));
        given(userRepository.findById(user1.getId()))
                .willReturn(Optional.of(user1));

        given(rateRepository.save(any(Rate.class)))
                .willAnswer(invocation -> {
                    Rate rate = invocation.getArgument(0);
                    rate.setId(1L);
                    return rate;
                });

        // When
        rateService.rateEpisode(user1.getId(), episode.getId(), 5);

        // Then
        verify(rateRepository).save(any(Rate.class));
    }

    @Test
    void rating이_있으면_수정() {
        // Given
        Rate existingRate = new Rate();
        existingRate.setRates(3);
        existingRate.setUser(user1);
        existingRate.setEpisode(episode);

        given(rateRepository.findByUserIdAndEpisodeId(user1.getId(), episode.getId()))
                .willReturn(Optional.of(existingRate));
        given(episodeRepository.findById(episode.getId()))
                .willReturn(Optional.of(episode));
        given(userRepository.findById(user1.getId()))
                .willReturn(Optional.of(user1));

        // When
        rateService.rateEpisode(user1.getId(), episode.getId(), 5);

        // Then
        verify(rateRepository).save(existingRate);
        assertThat(existingRate.getRates()).isEqualTo(5);
    }


    @Test
    void 평점_삭제_테스트() {
        // Given
        Rate rate = new Rate();
        rate.setId(1L);
        rate.setRates(4);
        rate.setUser(user1);
        rate.setEpisode(episode);

        given(episodeRepository.findById(episode.getId()))
                .willReturn(Optional.of(episode));

        given(rateRepository.findByUserIdAndEpisodeId(user1.getId(), episode.getId()))
                .willReturn(Optional.of(rate));

        // When
        rateService.deleteRate(user1.getId(), episode.getId());

        // Then
        verify(rateRepository).delete(rate);
    }

    @Test
    void 웹툰_평균_평점_반환() {
        // Given
        long webtoonId = 1L;
        double expectedAverageRate = 4.2;
        given(rateRepository.findAverageRateByWebtoonId(webtoonId))
                .willReturn(expectedAverageRate);

        // When
        double result = rateService.getAverageRateByWebtoonId(webtoonId);

        // Then
        assertThat(result).isEqualTo(expectedAverageRate);
    }

    @Test
    void 웹툰_평균_평점_없으면_0() {
        // Given
        long webtoonId = 1L;
        given(rateRepository.findAverageRateByWebtoonId(webtoonId))
                .willReturn(null);

        // When
        double result = rateService.getAverageRateByWebtoonId(webtoonId);

        // Then
        assertThat(result).isEqualTo(0.0);
    }

    @Test
    void 평점_삭제_시_에피소드가_존재하지_않으면_예외() {
        // Given
        Long userId = 1L;
        Long episodeId = 999L;
        given(episodeRepository.findById(episodeId)).willReturn(Optional.empty()); // Episode가 존재하지 않음

        // When & Then
        assertThatThrownBy(() -> rateService.deleteRate(userId, episodeId))
                .isInstanceOf(BaseException.class)
                .hasMessage("에피소드가 존재하지 않습니다."); // 예외 메시지 확인
    }


    @Test
    void 동시성_제어_테스트() throws InterruptedException {
        // Given

        given(rateRepository.findByUserIdAndEpisodeId(user1.getId(), episode.getId()))
                .willReturn(Optional.empty());
        given(rateRepository.findByUserIdAndEpisodeId(user2.getId(), episode.getId()))
                .willReturn(Optional.empty());

        given(episodeRepository.findById(episode.getId()))
                .willReturn(Optional.of(episode));
        given(userRepository.findById(user1.getId()))
                .willReturn(Optional.of(user1));
        given(userRepository.findById(user2.getId()))
                .willReturn(Optional.of(user2));

        given(rateRepository.save(any(Rate.class)))
                .willThrow(new OptimisticLockingFailureException("동시 수정 충돌 발생"));

        Thread thread1 = new Thread(() -> {
            assertThatThrownBy(() -> rateService.rateEpisode(user1.getId(), episode.getId(), 5))
                    .isInstanceOf(OptimisticLockingFailureException.class); // OptimisticLockingFailureException 발생
        });

        Thread thread2 = new Thread(() -> {
            assertThatThrownBy(() -> rateService.rateEpisode(user2.getId(), episode.getId(), 5))
                    .isInstanceOf(OptimisticLockingFailureException.class); // OptimisticLockingFailureException 발생
        });

        thread1.start();
        thread2.start();

        thread1.join();
        thread2.join();
    }

}

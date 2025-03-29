package com.example.toongallery.domain.rate.service;

import com.example.toongallery.domain.common.exception.BaseException;
import com.example.toongallery.domain.common.exception.ErrorCode;
import com.example.toongallery.domain.episode.entity.Episode;
import com.example.toongallery.domain.episode.repository.EpisodeRepository;
import com.example.toongallery.domain.rate.entity.Rate;
import com.example.toongallery.domain.rate.repository.RateRepository;
import com.example.toongallery.domain.user.entity.User;
import com.example.toongallery.domain.user.repository.UserRepository;
import com.example.toongallery.domain.webtoon.entity.Webtoon;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class RateService {

    private final RateRepository rateRepository;
    private final UserRepository userRepository;
    private final EpisodeRepository episodeRepository;

    private static final int MAX_RETRY_COUNT = 3;  // 최대 재시도 횟수
    private static final long RETRY_DELAY_MS = 100; // 재시도 간 대기 시간 (밀리초)
    private static final Random random = new Random(); // 랜덤 객체 생성

    @Transactional
    public void rateEpisode(Long userId, Long episodeId, int rate) {
        int retryCount = 0;

        while (retryCount < MAX_RETRY_COUNT) {
            try {
                Optional<Rate> optionalRate = rateRepository.findByUserIdAndEpisodeId(userId, episodeId);
                Episode episode = episodeRepository.findById(episodeId)
                        .orElseThrow(() -> new BaseException(ErrorCode.EPISODE_NOT_EXIST, null));
                User user = userRepository.findById(userId)
                        .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_EXIST, null));

                optionalRate.ifPresentOrElse(
                        existRate -> {
                            existRate.setRates(rate);
                            rateRepository.save(existRate);
                        },
                        () -> {
                            Rate newRate = Rate.builder()
                                    .rates(rate)
                                    .user(user)
                                    .episode(episode)
                                    .build();
                            rateRepository.save(newRate);
                        }
                );

                updateWebtoonAverageRate(episode.getWebtoon().getId());
                return;
            } catch (OptimisticLockingFailureException | OptimisticLockException e) {
                retryCount++;
                if (retryCount >= MAX_RETRY_COUNT) {
                    throw new RuntimeException("동시 수정 충돌 발생, 최대 재시도 횟수 초과");
                }

                try {
                    // 재시도 대기: 100ms ~ 150ms 사이의 랜덤한 시간 동안 대기
                    long retryDelay = RETRY_DELAY_MS + random.nextInt(51);
                    Thread.sleep(retryDelay);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt(); // 인터럽트 상태 복원
                    throw new RuntimeException("재시도 대기 중 인터럽트 발생", ie);
                }
            }
        }
    }

    @Transactional
    public void deleteRate(Long userId, Long episodeId) {
        // Episode가 존재하는지 확인
        episodeRepository.findById(episodeId)
                .orElseThrow(() -> new BaseException(ErrorCode.EPISODE_NOT_EXIST, null));

        // 사용자가 해당 에피소드에 남긴 평점이 있으면 삭제
        rateRepository.findByUserIdAndEpisodeId(userId, episodeId)
                .ifPresent(rateRepository::delete);
    }



    @Transactional
    public Double getAverageRateByWebtoonId(Long webtoonId) {
        return Optional.ofNullable(rateRepository.findAverageRateByWebtoonId(webtoonId))
                .orElse(0.0);
    }

    private void updateWebtoonAverageRate(Long webtoonId) {
        Double averageRate = rateRepository.findAverageRateByWebtoonId(webtoonId);
        if (averageRate != null) {
            Webtoon webtoon = episodeRepository.findById(webtoonId)
                    .orElseThrow(() -> new BaseException(ErrorCode.WEBTOON_NOT_FOUND,null))
                    .getWebtoon();
            webtoon.setRate(averageRate);
        }
    }
}

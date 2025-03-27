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

@Service
@RequiredArgsConstructor
public class RateService {

    private final RateRepository rateRepository;
    private final UserRepository userRepository;
    private final EpisodeRepository episodeRepository;

    private static final int MAX_RETRY_COUNT = 3;//재시도횟수

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
                    throw new BaseException(ErrorCode.CONCURRENCY_CONFLICT, null);
                }
            }
        }
    }

    @Transactional
    public void deleteRate(Long userId, Long episodeId) {
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
                    .orElseThrow(() -> new IllegalArgumentException("웹툰이 존재하지 않습니다."))
                    .getWebtoon();
            webtoon.setRate(averageRate);
        }
    }
}

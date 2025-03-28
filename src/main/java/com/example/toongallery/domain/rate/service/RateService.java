package com.example.toongallery.domain.rate.service;

import com.example.toongallery.domain.common.exception.BaseException;
import com.example.toongallery.domain.common.exception.ErrorCode;
import com.example.toongallery.domain.episode.entity.Episode;
import com.example.toongallery.domain.episode.repository.EpisodeRepository;
import com.example.toongallery.domain.rate.entity.Rate;
import com.example.toongallery.domain.rate.repository.RateRepository;
import com.example.toongallery.domain.user.entity.User;
import com.example.toongallery.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RateService {

    private final RateRepository rateRepository;
    private final UserRepository userRepository;
    private final EpisodeRepository episodeRepository;

    @Transactional
    public void rateEpisode(Long userId, Long episodeId, int rate) {

        Optional<Rate> optionalRate = rateRepository.findByUserIdAndEpisodeId(userId, episodeId);

        optionalRate.ifPresentOrElse(
                existRate -> existRate.setRates(rate), () -> {
                    Episode episode = episodeRepository.findById(episodeId)
                            .orElseThrow(() -> new BaseException(ErrorCode.EPISODE_NOT_FOUND, null));
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_EXIST, null));

                    Rate newRate = Rate.builder()
                            .rates(rate)
                            .user(user)
                            .episode(episode)
                            .build();

                    rateRepository.save(newRate);
                }
        );
    }


    @Transactional
    public void deleteRate(Long userId, Long episodeId) {
        rateRepository.findByUserIdAndEpisodeId(userId, episodeId)
                .ifPresent(rateRepository::delete);
    }

    public Double getAverageRate(Long episodeId) {
        return Optional.ofNullable(rateRepository.findAverageRateByEpisodeId(episodeId))
                .orElse(0.0);
    }
}

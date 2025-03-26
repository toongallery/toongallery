package com.example.toongallery.domain.episode.service;

import com.example.toongallery.domain.common.exception.BaseException;
import com.example.toongallery.domain.common.exception.ErrorCode;
import com.example.toongallery.domain.episode.entity.Episode;
import com.example.toongallery.domain.episode.repository.EpisodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EpisodeService {
    private final EpisodeRepository episodeRepository;
    public Episode getEpisode(Long episodeId) {
        return episodeRepository.findById(episodeId).orElseThrow(() ->
                new BaseException(ErrorCode.EPISODE_NOT_FOUND, null)
        );
    }
}

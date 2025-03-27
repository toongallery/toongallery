package com.example.toongallery.domain.common.util;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class FileName {
    private final String value;

    public static FileName forWebtoon() {
        return new FileName(UUID.randomUUID().toString());
    }

    public static FileName forEpisode(int no) {
        return new FileName(String.format("%04d_", no) + UUID.randomUUID());
    }
}

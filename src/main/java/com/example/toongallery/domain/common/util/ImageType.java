package com.example.toongallery.domain.common.util;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum ImageType {

    WEBTOON_THUMBNAIL("webtoons/%s/thumbnail/"),
    EPISODE_MAIN("webtoons/%s/episodes/main/%s"),
    EPISODE_THUMBNAIL("webtoons/%s/episodes/thumbnail/%s");

    private final String pathFormat;

    public String getPath(String... args) {
        return String.format(pathFormat, (Object[]) args);
    }

}

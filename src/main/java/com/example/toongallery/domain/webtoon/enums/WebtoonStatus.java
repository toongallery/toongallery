package com.example.toongallery.domain.webtoon.enums;

public enum WebtoonStatus {
    ONGOING("연재 중"),
    COMPLETED("완결"),
    HIATUS("휴재");

    private final String description;

    WebtoonStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

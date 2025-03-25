package com.example.toongallery.domain.user.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;


public enum Gender {
    MALE,FEMALE;
    public static Gender of(String gender) {
        return Arrays.stream(Gender.values())
                .filter(g -> g.name().equalsIgnoreCase(gender))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 Gender"));
    }
}

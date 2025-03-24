package com.example.toongallery.domain.user.enums;

import java.util.Arrays;

public enum UserStatus {
    ACTIVE,DELETE;

    public static UserStatus of(String status) {
        return Arrays.stream(UserStatus.values())
                .filter(s -> s.name().equalsIgnoreCase(status))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 UserStatus"));
    }
}

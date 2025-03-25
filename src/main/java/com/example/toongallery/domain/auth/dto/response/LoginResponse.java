package com.example.toongallery.domain.auth.dto.response;

import lombok.Getter;

@Getter
public class LoginResponse {

    private final String bearerToken;

    public LoginResponse(String bearerToken) {
        this.bearerToken = bearerToken;
    }
}

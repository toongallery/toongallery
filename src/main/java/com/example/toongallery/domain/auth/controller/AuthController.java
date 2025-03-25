package com.example.toongallery.domain.auth.controller;

import com.example.toongallery.domain.auth.dto.request.LoginRequest;
import com.example.toongallery.domain.auth.dto.request.SignupRequest;
import com.example.toongallery.domain.auth.dto.response.LoginResponse;
import com.example.toongallery.domain.auth.dto.response.SignupResponse;
import com.example.toongallery.domain.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public SignupResponse signup(@Valid @RequestBody SignupRequest signupRequest) {
        return authService.signup(signupRequest);
    }

    @PostMapping("/login")
    public LoginResponse signin(@Valid @RequestBody LoginRequest loginRequest) {
        return authService.login(loginRequest);
    }
}
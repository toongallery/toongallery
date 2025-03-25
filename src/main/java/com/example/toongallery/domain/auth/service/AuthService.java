package com.example.toongallery.domain.auth.service;

import com.example.toongallery.config.JwtUtil;
import com.example.toongallery.domain.auth.dto.request.LoginRequest;
import com.example.toongallery.domain.auth.dto.request.SignupRequest;
import com.example.toongallery.domain.auth.dto.response.LoginResponse;
import com.example.toongallery.domain.auth.dto.response.SignupResponse;
import com.example.toongallery.domain.user.entity.User;
import com.example.toongallery.domain.user.enums.Gender;
import com.example.toongallery.domain.user.enums.UserRole;
import com.example.toongallery.domain.user.enums.UserStatus;
import com.example.toongallery.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional
    public SignupResponse signup(SignupRequest signupRequest) {

        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            // 예외처리 추가 예정
        }

        String encodedPassword = passwordEncoder.encode(signupRequest.getPassword());

        UserRole userRole = UserRole.of(signupRequest.getUserRole());
        Gender gender = Gender.of(signupRequest.getGender());

        User newUser = new User(
                signupRequest.getEmail(),
                encodedPassword,
                signupRequest.getName(),
                signupRequest.getBirthDate(),
                gender,
                userRole,
                UserStatus.ACTIVE
        );
        User savedUser = userRepository.save(newUser);

        String bearerToken = jwtUtil.createToken(savedUser.getId(), savedUser.getEmail(), userRole);

        return new SignupResponse(bearerToken);
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest loginRequest) {
        User user = userRepository.findByEmail(loginRequest.getEmail()).orElseThrow(
                () -> new RuntimeException("가입되지 않은 유저입니다.")); // 예외처리 수정 예정

        // 로그인 시 이메일과 비밀번호가 일치하지 않을 경우 401을 반환합니다.
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            // 예외처리 추가 예정
        }

        String bearerToken = jwtUtil.createToken(user.getId(), user.getEmail(), user.getUserRole());

        return new LoginResponse(bearerToken);
    }
}

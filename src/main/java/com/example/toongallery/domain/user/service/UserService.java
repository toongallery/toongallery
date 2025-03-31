package com.example.toongallery.domain.user.service;

import com.example.toongallery.domain.common.exception.BaseException;
import com.example.toongallery.domain.common.exception.ErrorCode;
import com.example.toongallery.domain.user.dto.request.PasswordChangeRequest;
import com.example.toongallery.domain.user.dto.request.UserUpdateRequest;
import com.example.toongallery.domain.user.dto.response.UserResponse;
import com.example.toongallery.domain.user.entity.User;
import com.example.toongallery.domain.user.enums.Gender;
import com.example.toongallery.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public UserResponse getUser(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_EXIST, null));
        return UserResponse.of(user);
    }

    @Transactional
    public UserResponse updateUser(Long userId, UserUpdateRequest request) {
        User user = userRepository.findById(userId).orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_EXIST, null));

        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            user.updateEmail(request.getEmail());
        }
        if (request.getName() != null && !request.getName().isBlank()) {
            user.updateName(request.getName());
        }
        if (request.getGender() != null && !request.getGender().isBlank()) {
            Gender gender = Gender.of(request.getGender());
            user.updateGender(gender);
        }
        return UserResponse.of(user);
    }

    @Transactional
    public void updatePassword(Long userId, PasswordChangeRequest request) {
        User user = userRepository.findById(userId).orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_EXIST, null));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BaseException(ErrorCode.PASSWORD_MISMATCH, null);
        }
        if (!request.getNewPassword().equals(request.getNewCheckPassword())) {
            throw new BaseException(ErrorCode.PASSWORD_NOT_SAME, null);
        }
        if (request.getOldPassword().equals(request.getNewPassword())) {
            throw new BaseException(ErrorCode.PASSWORD_SAME_AS_OLD, null);
        }
        user.changePassword(passwordEncoder.encode(request.getNewPassword()));
    }

    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_EXIST, null));
        user.delete();
    }
}

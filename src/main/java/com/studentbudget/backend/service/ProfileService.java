package com.studentbudget.backend.service;

import com.studentbudget.backend.dto.ChangePasswordRequest;
import com.studentbudget.backend.dto.ProfileResponse;
import com.studentbudget.backend.dto.UpdateProfileRequest;

import com.studentbudget.backend.entity.User;
import com.studentbudget.backend.exception.BadRequestException;
import com.studentbudget.backend.exception.ResourceNotFoundException;
import com.studentbudget.backend.repository.UserRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProfileService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public ProfileService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public ProfileResponse getProfile(String email) {

        User user = getCurrentUser(email);

        return buildResponse(user);
    }

    @Transactional
    public ProfileResponse updateProfile(
            String email,
            UpdateProfileRequest request) {

        User user = getCurrentUser(email);

        String newEmail =
                request.email().trim().toLowerCase();

        if (!user.getEmail().equals(newEmail)
                && userRepository.existsByEmail(newEmail)) {

            throw new BadRequestException(
                    "Email is already registered"
            );
        }

        user.setName(request.name().trim());
        user.setEmail(newEmail);

        User savedUser = userRepository.save(user);

        return buildResponse(savedUser);
    }

    @Transactional
    public void changePassword(
            String email,
            ChangePasswordRequest request) {

        User user = getCurrentUser(email);

        if (!passwordEncoder.matches(
                request.currentPassword(),
                user.getPassword())) {

            throw new BadRequestException(
                    "Current password is incorrect"
            );
        }

        if (passwordEncoder.matches(
                request.newPassword(),
                user.getPassword())) {

            throw new BadRequestException(
                    "New password must be different"
            );
        }

        user.setPassword(
                passwordEncoder.encode(
                        request.newPassword()
                )
        );

        userRepository.save(user);
    }

    private User getCurrentUser(String email) {

        return userRepository
                .findByEmail(email.trim().toLowerCase())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found"
                        )
                );
    }

    private ProfileResponse buildResponse(User user) {

        return new ProfileResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole().name(),
                user.getCreatedAt()
        );
    }
}
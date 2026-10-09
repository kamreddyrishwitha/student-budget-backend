package com.studentbudget.backend.controller;

import com.studentbudget.backend.dto.ChangePasswordRequest;
import com.studentbudget.backend.dto.ProfileResponse;
import com.studentbudget.backend.dto.UpdateProfileRequest;

import com.studentbudget.backend.service.ProfileService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping
    public ResponseEntity<ProfileResponse> getProfile(
            Principal principal) {

        return ResponseEntity.ok(
                profileService.getProfile(
                        principal.getName()
                )
        );
    }

    @PutMapping
    public ResponseEntity<ProfileResponse> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request,
            Principal principal) {

        return ResponseEntity.ok(
                profileService.updateProfile(
                        principal.getName(),
                        request
                )
        );
    }

    @PutMapping("/change-password")
    public ResponseEntity<Map<String, String>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            Principal principal) {

        profileService.changePassword(
                principal.getName(),
                request
        );

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "Password changed successfully"
                )
        );
    }
}
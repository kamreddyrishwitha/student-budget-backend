package com.studentbudget.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AuthResponse {

    private String message;
    private Long userId;
    private String name;
    private String email;
    private String role;
    private String token;
}
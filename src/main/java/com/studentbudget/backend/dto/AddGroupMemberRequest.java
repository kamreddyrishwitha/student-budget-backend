package com.studentbudget.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record AddGroupMemberRequest(

        @NotBlank(message = "Member email is required")
        @Email(message = "Enter a valid email address")
        String email

) {
}
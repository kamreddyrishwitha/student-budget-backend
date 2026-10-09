package com.studentbudget.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record GroupRequest(

        @NotBlank(message = "Group name is required")
        @Size(
                max = 150,
                message = "Group name cannot exceed 150 characters"
        )
        String name,

        @Size(
                max = 500,
                message = "Description cannot exceed 500 characters"
        )
        String description

) {
}
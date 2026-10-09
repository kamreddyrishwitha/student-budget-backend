package com.studentbudget.backend.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record SplitParticipantRequest(

        @NotNull(message = "User ID is required")
        Long userId,

        @NotNull(message = "Split value is required")
        @DecimalMin(
                value = "0.01",
                message = "Split value must be greater than zero"
        )
        BigDecimal value

) {
}
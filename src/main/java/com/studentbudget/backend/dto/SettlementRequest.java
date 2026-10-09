package com.studentbudget.backend.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SettlementRequest(

        @NotNull(message = "From user ID is required")
        Long fromUserId,

        @NotNull(message = "To user ID is required")
        Long toUserId,

        @NotNull(message = "Amount is required")
        @DecimalMin(
                value = "0.01",
                message = "Amount must be greater than zero"
        )
        BigDecimal amount,

        @NotNull(message = "Settlement date is required")
        LocalDate settlementDate,

        @Size(
                max = 500,
                message = "Note cannot exceed 500 characters"
        )
        String note

) {
}
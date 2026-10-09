package com.studentbudget.backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record CustomSplitRequest(

        @NotBlank(message = "Title is required")
        @Size(
                max = 150,
                message = "Title cannot exceed 150 characters"
        )
        String title,

        @NotNull(message = "Amount is required")
        @DecimalMin(
                value = "0.01",
                message = "Amount must be greater than zero"
        )
        BigDecimal amount,

        @Size(
                max = 500,
                message = "Description cannot exceed 500 characters"
        )
        String description,

        @NotNull(message = "Expense date is required")
        LocalDate expenseDate,

        @NotNull(message = "Paid by user is required")
        Long paidByUserId,

        @NotEmpty(message = "At least one participant is required")
        List<@Valid SplitParticipantRequest> participants

) {
}
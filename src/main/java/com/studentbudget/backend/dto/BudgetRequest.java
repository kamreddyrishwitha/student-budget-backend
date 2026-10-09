package com.studentbudget.backend.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record BudgetRequest(

        @NotNull(message = "Month is required")
        @Min(value = 1, message = "Month must be between 1 and 12")
        @Max(value = 12, message = "Month must be between 1 and 12")
        Integer month,

        @NotNull(message = "Year is required")
        @Min(value = 2000, message = "Enter a valid year")
        Integer year,

        @NotNull(message = "Budget amount is required")
        @DecimalMin(
                value = "0.01",
                message = "Budget amount must be greater than zero"
        )
        BigDecimal amount

) {
}
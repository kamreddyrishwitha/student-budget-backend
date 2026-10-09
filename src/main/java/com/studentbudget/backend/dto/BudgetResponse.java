package com.studentbudget.backend.dto;

import java.math.BigDecimal;

public record BudgetResponse(

        Long id,
        Integer month,
        Integer year,
        BigDecimal budgetAmount,
        BigDecimal amountSpent,
        BigDecimal remainingAmount,
        BigDecimal usagePercentage,
        String status

) {
}
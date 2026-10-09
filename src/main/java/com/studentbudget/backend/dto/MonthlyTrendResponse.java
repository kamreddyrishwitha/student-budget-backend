package com.studentbudget.backend.dto;

import java.math.BigDecimal;

public record MonthlyTrendResponse(
        Integer month,
        Integer year,
        String monthName,
        BigDecimal income,
        BigDecimal expenses,
        BigDecimal savings
) {
}
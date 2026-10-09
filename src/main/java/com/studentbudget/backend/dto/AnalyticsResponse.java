package com.studentbudget.backend.dto;

import java.math.BigDecimal;
import java.util.List;

public record AnalyticsResponse(
        Integer month,
        Integer year,

        BigDecimal totalIncome,
        BigDecimal totalExpenses,
        BigDecimal savings,
        BigDecimal savingsRate,

        BigDecimal budgetAmount,
        BigDecimal remainingBudget,
        BigDecimal budgetUsagePercentage,
        String budgetStatus,

        List<CategorySpendingResponse> categorySpending,
        List<MonthlyTrendResponse> monthlyTrend
) {
}
package com.studentbudget.backend.dto;

import java.math.BigDecimal;
import java.util.List;

public record DashboardResponse(

        Integer month,
        Integer year,

        BigDecimal totalIncome,
        BigDecimal totalExpenses,
        BigDecimal balance,

        BigDecimal budgetAmount,
        BigDecimal remainingBudget,
        BigDecimal budgetUsagePercentage,
        String budgetStatus,

        List<TransactionResponse> recentTransactions,

        List<CategorySpendingResponse> categorySpending

) {
}
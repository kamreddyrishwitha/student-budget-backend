package com.studentbudget.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record SharedExpenseResponse(

        Long id,
        Long groupId,
        String groupName,
        String title,
        BigDecimal amount,
        String description,
        LocalDate expenseDate,
        String splitType,
        Long paidByUserId,
        String paidByName,
        LocalDateTime createdAt,
        List<ExpenseSplitResponse> splits

) {
}
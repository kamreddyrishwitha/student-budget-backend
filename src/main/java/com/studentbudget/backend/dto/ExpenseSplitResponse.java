package com.studentbudget.backend.dto;

import java.math.BigDecimal;

public record ExpenseSplitResponse(

        Long userId,
        String name,
        String email,
        BigDecimal shareAmount,
        BigDecimal settledAmount,
        BigDecimal pendingAmount

) {
}
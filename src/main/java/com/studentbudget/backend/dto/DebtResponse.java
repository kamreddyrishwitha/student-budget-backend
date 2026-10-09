package com.studentbudget.backend.dto;

import java.math.BigDecimal;

public record DebtResponse(
        Long fromUserId,
        String fromUserName,
        Long toUserId,
        String toUserName,
        BigDecimal amount
) {
}
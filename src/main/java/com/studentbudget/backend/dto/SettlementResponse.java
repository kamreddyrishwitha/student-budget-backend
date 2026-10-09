package com.studentbudget.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record SettlementResponse(

        Long id,
        Long groupId,

        Long fromUserId,
        String fromUserName,

        Long toUserId,
        String toUserName,

        BigDecimal amount,
        LocalDate settlementDate,
        String note,
        LocalDateTime createdAt

) {
}
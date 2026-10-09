package com.studentbudget.backend.dto;

import java.util.List;

public record GroupBalanceResponse(
        Long groupId,
        String groupName,
        List<DebtResponse> debts
) {
}
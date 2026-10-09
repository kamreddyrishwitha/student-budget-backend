package com.studentbudget.backend.dto;

import java.math.BigDecimal;

public record CategorySpendingResponse(

        Long categoryId,
        String categoryName,
        BigDecimal amount

) {
}
package com.studentbudget.backend.dto;

import java.time.LocalDateTime;
import java.util.List;

public record GroupResponse(

        Long id,
        String name,
        String description,
        Long createdByUserId,
        String createdByName,
        LocalDateTime createdAt,
        Integer memberCount,
        List<GroupMemberResponse> members

) {
}
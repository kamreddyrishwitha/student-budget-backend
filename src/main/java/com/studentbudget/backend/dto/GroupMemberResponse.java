package com.studentbudget.backend.dto;

import java.time.LocalDateTime;

public record GroupMemberResponse(

        Long membershipId,
        Long userId,
        String name,
        String email,
        LocalDateTime joinedAt

) {
}
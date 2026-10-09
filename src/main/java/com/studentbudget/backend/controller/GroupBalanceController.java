package com.studentbudget.backend.controller;

import com.studentbudget.backend.dto.GroupBalanceResponse;
import com.studentbudget.backend.service.GroupBalanceService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/groups/{groupId}/balances")
public class GroupBalanceController {

    private final GroupBalanceService groupBalanceService;

    public GroupBalanceController(
            GroupBalanceService groupBalanceService) {

        this.groupBalanceService =
                groupBalanceService;
    }

    @GetMapping
    public ResponseEntity<GroupBalanceResponse>
    getGroupBalances(
            @PathVariable Long groupId,
            Principal principal) {

        return ResponseEntity.ok(
                groupBalanceService
                        .getGroupBalances(
                                principal.getName(),
                                groupId
                        )
        );
    }
}
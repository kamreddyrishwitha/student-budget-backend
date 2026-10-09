package com.studentbudget.backend.controller;

import com.studentbudget.backend.dto.CustomSplitRequest;
import com.studentbudget.backend.dto.PercentageSplitRequest;
import com.studentbudget.backend.dto.SharedExpenseRequest;
import com.studentbudget.backend.dto.SharedExpenseResponse;
import com.studentbudget.backend.service.SharedExpenseService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/groups/{groupId}/expenses")
public class SharedExpenseController {

    private final SharedExpenseService sharedExpenseService;

    public SharedExpenseController(
            SharedExpenseService sharedExpenseService) {

        this.sharedExpenseService =
                sharedExpenseService;
    }

    @PostMapping("/equal")
    public ResponseEntity<SharedExpenseResponse>
    createEqualSplitExpense(
            @PathVariable Long groupId,
            @Valid
            @RequestBody
            SharedExpenseRequest request,
            Principal principal) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        sharedExpenseService
                                .createEqualSplitExpense(
                                        principal.getName(),
                                        groupId,
                                        request
                                )
                );
    }

    @PostMapping("/custom")
    public ResponseEntity<SharedExpenseResponse>
    createCustomSplitExpense(
            @PathVariable Long groupId,
            @Valid
            @RequestBody
            CustomSplitRequest request,
            Principal principal) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        sharedExpenseService
                                .createCustomSplitExpense(
                                        principal.getName(),
                                        groupId,
                                        request
                                )
                );
    }

    @PostMapping("/percentage")
    public ResponseEntity<SharedExpenseResponse>
    createPercentageSplitExpense(
            @PathVariable Long groupId,
            @Valid
            @RequestBody
            PercentageSplitRequest request,
            Principal principal) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        sharedExpenseService
                                .createPercentageSplitExpense(
                                        principal.getName(),
                                        groupId,
                                        request
                                )
                );
    }

    @GetMapping
    public ResponseEntity<List<SharedExpenseResponse>>
    getGroupExpenses(
            @PathVariable Long groupId,
            Principal principal) {

        return ResponseEntity.ok(
                sharedExpenseService
                        .getGroupExpenses(
                                principal.getName(),
                                groupId
                        )
        );
    }

    @GetMapping("/{expenseId}")
    public ResponseEntity<SharedExpenseResponse>
    getExpenseById(
            @PathVariable Long groupId,
            @PathVariable Long expenseId,
            Principal principal) {

        return ResponseEntity.ok(
                sharedExpenseService
                        .getExpenseById(
                                principal.getName(),
                                groupId,
                                expenseId
                        )
        );
    }
}
package com.studentbudget.backend.controller;

import com.studentbudget.backend.dto.BudgetRequest;
import com.studentbudget.backend.dto.BudgetResponse;
import com.studentbudget.backend.service.BudgetService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/budgets")
public class BudgetController {

    private final BudgetService budgetService;

    public BudgetController(
            BudgetService budgetService) {

        this.budgetService = budgetService;
    }

    @PostMapping
    public ResponseEntity<BudgetResponse> createBudget(
            @Valid @RequestBody BudgetRequest request,
            Principal principal) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        budgetService.createBudget(
                                principal.getName(),
                                request
                        )
                );
    }

    @GetMapping
    public ResponseEntity<List<BudgetResponse>>
    getAllBudgets(
            Principal principal) {

        return ResponseEntity.ok(
                budgetService.getAllBudgets(
                        principal.getName()
                )
        );
    }

    @GetMapping("/monthly")
    public ResponseEntity<BudgetResponse>
    getMonthlyBudget(
            @RequestParam Integer month,
            @RequestParam Integer year,
            Principal principal) {

        return ResponseEntity.ok(
                budgetService.getBudget(
                        principal.getName(),
                        month,
                        year
                )
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<BudgetResponse>
    updateBudget(
            @PathVariable Long id,
            @Valid @RequestBody BudgetRequest request,
            Principal principal) {

        return ResponseEntity.ok(
                budgetService.updateBudget(
                        principal.getName(),
                        id,
                        request
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>>
    deleteBudget(
            @PathVariable Long id,
            Principal principal) {

        budgetService.deleteBudget(
                principal.getName(),
                id
        );

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "Budget deleted successfully"
                )
        );
    }
}
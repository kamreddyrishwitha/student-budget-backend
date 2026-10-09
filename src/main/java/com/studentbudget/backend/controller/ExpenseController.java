package com.studentbudget.backend.controller;

import com.studentbudget.backend.dto.ExpenseRequest;
import com.studentbudget.backend.entity.Expense;
import com.studentbudget.backend.service.ExpenseService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(
            ExpenseService expenseService) {

        this.expenseService = expenseService;
    }

    @PostMapping
    public ResponseEntity<Expense> addExpense(
            @Valid @RequestBody ExpenseRequest request,
            Principal principal) {

        Expense expense =
                expenseService.addExpense(
                        principal.getName(),
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(expense);
    }

    @GetMapping
    public ResponseEntity<List<Expense>>
    getAllExpenses(
            Principal principal) {

        return ResponseEntity.ok(
                expenseService.getAllExpenses(
                        principal.getName()
                )
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<Expense> getExpenseById(
            @PathVariable Long id,
            Principal principal) {

        return ResponseEntity.ok(
                expenseService.getExpenseById(
                        principal.getName(),
                        id
                )
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<Expense> updateExpense(
            @PathVariable Long id,
            @Valid @RequestBody ExpenseRequest request,
            Principal principal) {

        return ResponseEntity.ok(
                expenseService.updateExpense(
                        principal.getName(),
                        id,
                        request
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>>
    deleteExpense(
            @PathVariable Long id,
            Principal principal) {

        expenseService.deleteExpense(
                principal.getName(),
                id
        );

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "Expense deleted successfully"
                )
        );
    }
}
package com.studentbudget.backend.controller;

import com.studentbudget.backend.dto.IncomeRequest;
import com.studentbudget.backend.entity.Income;
import com.studentbudget.backend.service.IncomeService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/incomes")
public class IncomeController {

    private final IncomeService incomeService;

    public IncomeController(
            IncomeService incomeService) {

        this.incomeService = incomeService;
    }

    @PostMapping
    public ResponseEntity<Income> addIncome(
            @Valid @RequestBody IncomeRequest request,
            Principal principal) {

        Income income =
                incomeService.addIncome(
                        principal.getName(),
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(income);
    }

    @GetMapping
    public ResponseEntity<List<Income>> getAllIncomes(
            Principal principal) {

        return ResponseEntity.ok(
                incomeService.getAllIncomes(
                        principal.getName()
                )
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<Income> getIncomeById(
            @PathVariable Long id,
            Principal principal) {

        return ResponseEntity.ok(
                incomeService.getIncomeById(
                        principal.getName(),
                        id
                )
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<Income> updateIncome(
            @PathVariable Long id,
            @Valid @RequestBody IncomeRequest request,
            Principal principal) {

        return ResponseEntity.ok(
                incomeService.updateIncome(
                        principal.getName(),
                        id,
                        request
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteIncome(
            @PathVariable Long id,
            Principal principal) {

        incomeService.deleteIncome(
                principal.getName(),
                id
        );

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "Income deleted successfully"
                )
        );
    }
}
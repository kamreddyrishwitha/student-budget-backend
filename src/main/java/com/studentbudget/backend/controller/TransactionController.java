package com.studentbudget.backend.controller;

import com.studentbudget.backend.dto.TransactionResponse;
import com.studentbudget.backend.service.TransactionService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(
            TransactionService transactionService) {

        this.transactionService = transactionService;
    }

    @GetMapping
    public ResponseEntity<List<TransactionResponse>>
    getTransactions(
            @RequestParam(required = false)
            String type,

            @RequestParam(required = false)
            Integer month,

            @RequestParam(required = false)
            Integer year,

            @RequestParam(required = false)
            Long categoryId,

            Principal principal) {

        return ResponseEntity.ok(
                transactionService.getTransactions(
                        principal.getName(),
                        type,
                        month,
                        year,
                        categoryId
                )
        );
    }
}
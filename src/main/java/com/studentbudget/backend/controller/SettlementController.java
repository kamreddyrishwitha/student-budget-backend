package com.studentbudget.backend.controller;

import com.studentbudget.backend.dto.SettlementRequest;
import com.studentbudget.backend.dto.SettlementResponse;
import com.studentbudget.backend.service.SettlementService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/groups/{groupId}/settlements")
public class SettlementController {

    private final SettlementService settlementService;

    public SettlementController(
            SettlementService settlementService) {

        this.settlementService =
                settlementService;
    }

    @PostMapping
    public ResponseEntity<SettlementResponse>
    createSettlement(
            @PathVariable Long groupId,
            @Valid
            @RequestBody SettlementRequest request,
            Principal principal) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        settlementService
                                .createSettlement(
                                        principal.getName(),
                                        groupId,
                                        request
                                )
                );
    }

    @GetMapping
    public ResponseEntity<List<SettlementResponse>>
    getGroupSettlements(
            @PathVariable Long groupId,
            Principal principal) {

        return ResponseEntity.ok(
                settlementService
                        .getGroupSettlements(
                                principal.getName(),
                                groupId
                        )
        );
    }
}
package com.studentbudget.backend.controller;

import com.studentbudget.backend.dto.DashboardResponse;
import com.studentbudget.backend.service.DashboardService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(
            DashboardService dashboardService) {

        this.dashboardService = dashboardService;
    }

    @GetMapping
    public ResponseEntity<DashboardResponse>
    getDashboard(
            @RequestParam Integer month,
            @RequestParam Integer year,
            Principal principal) {

        return ResponseEntity.ok(
                dashboardService.getDashboard(
                        principal.getName(),
                        month,
                        year
                )
        );
    }
}
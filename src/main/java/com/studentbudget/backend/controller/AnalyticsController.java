package com.studentbudget.backend.controller;

import com.studentbudget.backend.dto.AnalyticsResponse;
import com.studentbudget.backend.service.AnalyticsService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(
            AnalyticsService analyticsService) {

        this.analyticsService =
                analyticsService;
    }

    @GetMapping
    public ResponseEntity<AnalyticsResponse>
    getAnalytics(
            @RequestParam(required = false)
            Integer month,

            @RequestParam(required = false)
            Integer year,

            Principal principal) {

        LocalDate today =
                LocalDate.now();

        int selectedMonth =
                month != null
                        ? month
                        : today.getMonthValue();

        int selectedYear =
                year != null
                        ? year
                        : today.getYear();

        return ResponseEntity.ok(
                analyticsService
                        .getAnalytics(
                                principal.getName(),
                                selectedMonth,
                                selectedYear
                        )
        );
    }
}
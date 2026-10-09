package com.studentbudget.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @GetMapping
    public ResponseEntity<Map<String, String>> test(
            Principal principal) {

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "JWT authentication is working!",
                        "user",
                        principal.getName()
                )
        );
    }
}
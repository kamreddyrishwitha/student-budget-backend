package com.studentbudget.backend.controller;

import com.studentbudget.backend.dto.CategoryRequest;
import com.studentbudget.backend.entity.Category;
import com.studentbudget.backend.service.CategoryService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(
            CategoryService categoryService) {

        this.categoryService = categoryService;
    }

    @PostMapping
    public ResponseEntity<Category> addCategory(
            @Valid @RequestBody CategoryRequest request,
            Principal principal) {

        Category category =
                categoryService.addCategory(
                        principal.getName(),
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(category);
    }

    @GetMapping
    public ResponseEntity<List<Category>>
    getAllCategories(
            Principal principal) {

        return ResponseEntity.ok(
                categoryService.getAllCategories(
                        principal.getName()
                )
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<Category> getCategoryById(
            @PathVariable Long id,
            Principal principal) {

        return ResponseEntity.ok(
                categoryService.getCategoryById(
                        principal.getName(),
                        id
                )
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<Category> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequest request,
            Principal principal) {

        return ResponseEntity.ok(
                categoryService.updateCategory(
                        principal.getName(),
                        id,
                        request
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>>
    deleteCategory(
            @PathVariable Long id,
            Principal principal) {

        categoryService.deleteCategory(
                principal.getName(),
                id
        );

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "Category deleted successfully"
                )
        );
    }
}
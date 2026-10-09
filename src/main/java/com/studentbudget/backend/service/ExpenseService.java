package com.studentbudget.backend.service;

import com.studentbudget.backend.dto.ExpenseRequest;
import com.studentbudget.backend.entity.Category;
import com.studentbudget.backend.entity.Expense;
import com.studentbudget.backend.entity.User;
import com.studentbudget.backend.exception.ResourceNotFoundException;
import com.studentbudget.backend.repository.CategoryRepository;
import com.studentbudget.backend.repository.ExpenseRepository;
import com.studentbudget.backend.repository.UserRepository;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    public ExpenseService(
            ExpenseRepository expenseRepository,
            UserRepository userRepository,
            CategoryRepository categoryRepository) {

        this.expenseRepository = expenseRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
    }

    private User getCurrentUser(String email) {

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Authenticated user not found"
                        )
                );
    }

    private Category getCategory(
            User user,
            Long categoryId) {

        return categoryRepository
                .findByIdAndUser(categoryId, user)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Category not found"
                        )
                );
    }

    public Expense addExpense(
            String email,
            ExpenseRequest request) {

        User user = getCurrentUser(email);

        Category category =
                getCategory(
                        user,
                        request.categoryId()
                );

        Expense expense = Expense.builder()
                .title(request.title().trim())
                .amount(request.amount())
                .description(
                        cleanDescription(
                                request.description()
                        )
                )
                .expenseDate(request.expenseDate())
                .category(category)
                .user(user)
                .build();

        return expenseRepository.save(expense);
    }

    public List<Expense> getAllExpenses(
            String email) {

        User user = getCurrentUser(email);

        return expenseRepository
                .findByUserOrderByExpenseDateDesc(user);
    }

    public Expense getExpenseById(
            String email,
            Long id) {

        User user = getCurrentUser(email);

        return expenseRepository
                .findByIdAndUser(id, user)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Expense not found"
                        )
                );
    }

    public Expense updateExpense(
            String email,
            Long id,
            ExpenseRequest request) {

        User user = getCurrentUser(email);

        Expense expense =
                expenseRepository
                        .findByIdAndUser(id, user)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Expense not found"
                                )
                        );

        Category category =
                getCategory(
                        user,
                        request.categoryId()
                );

        expense.setTitle(
                request.title().trim()
        );

        expense.setAmount(
                request.amount()
        );

        expense.setDescription(
                cleanDescription(
                        request.description()
                )
        );

        expense.setExpenseDate(
                request.expenseDate()
        );

        expense.setCategory(category);

        return expenseRepository.save(expense);
    }

    public void deleteExpense(
            String email,
            Long id) {

        User user = getCurrentUser(email);

        Expense expense =
                expenseRepository
                        .findByIdAndUser(id, user)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Expense not found"
                                )
                        );

        expenseRepository.delete(expense);
    }

    private String cleanDescription(
            String description) {

        if (description == null) {
            return null;
        }

        String cleaned =
                description.trim();

        return cleaned.isEmpty()
                ? null
                : cleaned;
    }
}
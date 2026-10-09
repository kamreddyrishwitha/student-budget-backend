package com.studentbudget.backend.service;

import com.studentbudget.backend.dto.BudgetRequest;
import com.studentbudget.backend.dto.BudgetResponse;
import com.studentbudget.backend.entity.Budget;
import com.studentbudget.backend.entity.Expense;
import com.studentbudget.backend.entity.User;
import com.studentbudget.backend.exception.BadRequestException;
import com.studentbudget.backend.exception.ResourceNotFoundException;
import com.studentbudget.backend.repository.BudgetRepository;
import com.studentbudget.backend.repository.ExpenseRepository;
import com.studentbudget.backend.repository.UserRepository;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;

    public BudgetService(
            BudgetRepository budgetRepository,
            ExpenseRepository expenseRepository,
            UserRepository userRepository) {

        this.budgetRepository = budgetRepository;
        this.expenseRepository = expenseRepository;
        this.userRepository = userRepository;
    }

    private User getCurrentUser(String email) {

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Authenticated user not found"
                        )
                );
    }

    public BudgetResponse createBudget(
            String email,
            BudgetRequest request) {

        User user = getCurrentUser(email);

        if (budgetRepository
                .findByUserAndMonthAndYear(
                        user,
                        request.month(),
                        request.year()
                )
                .isPresent()) {

            throw new BadRequestException(
                    "Budget already exists for this month"
            );
        }

        Budget budget = Budget.builder()
                .month(request.month())
                .year(request.year())
                .amount(request.amount())
                .user(user)
                .build();

        Budget savedBudget =
                budgetRepository.save(budget);

        return buildResponse(
                savedBudget,
                user
        );
    }

    public List<BudgetResponse> getAllBudgets(
            String email) {

        User user = getCurrentUser(email);

        return budgetRepository
                .findByUserOrderByYearDescMonthDesc(user)
                .stream()
                .map(budget ->
                        buildResponse(
                                budget,
                                user
                        )
                )
                .toList();
    }

    public BudgetResponse getBudget(
            String email,
            Integer month,
            Integer year) {

        User user = getCurrentUser(email);

        Budget budget = budgetRepository
                .findByUserAndMonthAndYear(
                        user,
                        month,
                        year
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Budget not found"
                        )
                );

        return buildResponse(
                budget,
                user
        );
    }

    public BudgetResponse updateBudget(
            String email,
            Long id,
            BudgetRequest request) {

        User user = getCurrentUser(email);

        Budget budget = budgetRepository
                .findByIdAndUser(id, user)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Budget not found"
                        )
                );

        budgetRepository
                .findByUserAndMonthAndYear(
                        user,
                        request.month(),
                        request.year()
                )
                .ifPresent(existingBudget -> {

                    if (!existingBudget
                            .getId()
                            .equals(id)) {

                        throw new BadRequestException(
                                "Budget already exists for this month"
                        );
                    }
                });

        budget.setMonth(request.month());
        budget.setYear(request.year());
        budget.setAmount(request.amount());

        Budget savedBudget =
                budgetRepository.save(budget);

        return buildResponse(
                savedBudget,
                user
        );
    }

    public void deleteBudget(
            String email,
            Long id) {

        User user = getCurrentUser(email);

        Budget budget = budgetRepository
                .findByIdAndUser(id, user)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Budget not found"
                        )
                );

        budgetRepository.delete(budget);
    }

    private BudgetResponse buildResponse(
            Budget budget,
            User user) {

        YearMonth yearMonth =
                YearMonth.of(
                        budget.getYear(),
                        budget.getMonth()
                );

        LocalDate startDate =
                yearMonth.atDay(1);

        LocalDate endDate =
                yearMonth.atEndOfMonth();

        List<Expense> expenses =
                expenseRepository
                        .findByUserAndExpenseDateBetween(
                                user,
                                startDate,
                                endDate
                        );

        BigDecimal amountSpent =
                expenses.stream()
                        .map(Expense::getAmount)
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        BigDecimal remainingAmount =
                budget.getAmount()
                        .subtract(amountSpent);

        BigDecimal usagePercentage =
                amountSpent
                        .multiply(
                                BigDecimal.valueOf(100)
                        )
                        .divide(
                                budget.getAmount(),
                                2,
                                RoundingMode.HALF_UP
                        );

        String status =
                calculateStatus(
                        usagePercentage
                );

        return new BudgetResponse(
                budget.getId(),
                budget.getMonth(),
                budget.getYear(),
                budget.getAmount(),
                amountSpent,
                remainingAmount,
                usagePercentage,
                status
        );
    }

    private String calculateStatus(
            BigDecimal usagePercentage) {

        if (usagePercentage.compareTo(
                BigDecimal.valueOf(100)) >= 0) {

            return "EXCEEDED";
        }

        if (usagePercentage.compareTo(
                BigDecimal.valueOf(90)) >= 0) {

            return "NEAR_LIMIT";
        }

        if (usagePercentage.compareTo(
                BigDecimal.valueOf(75)) >= 0) {

            return "WARNING";
        }

        return "SAFE";
    }
}
package com.studentbudget.backend.service;

import com.studentbudget.backend.dto.CategorySpendingResponse;
import com.studentbudget.backend.dto.DashboardResponse;
import com.studentbudget.backend.dto.TransactionResponse;
import com.studentbudget.backend.entity.Budget;
import com.studentbudget.backend.entity.Expense;
import com.studentbudget.backend.entity.Income;
import com.studentbudget.backend.entity.User;
import com.studentbudget.backend.exception.BadRequestException;
import com.studentbudget.backend.exception.ResourceNotFoundException;
import com.studentbudget.backend.repository.BudgetRepository;
import com.studentbudget.backend.repository.ExpenseRepository;
import com.studentbudget.backend.repository.IncomeRepository;
import com.studentbudget.backend.repository.UserRepository;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardService {

    private final UserRepository userRepository;
    private final IncomeRepository incomeRepository;
    private final ExpenseRepository expenseRepository;
    private final BudgetRepository budgetRepository;

    public DashboardService(
            UserRepository userRepository,
            IncomeRepository incomeRepository,
            ExpenseRepository expenseRepository,
            BudgetRepository budgetRepository) {

        this.userRepository = userRepository;
        this.incomeRepository = incomeRepository;
        this.expenseRepository = expenseRepository;
        this.budgetRepository = budgetRepository;
    }

    public DashboardResponse getDashboard(
            String email,
            Integer month,
            Integer year) {

        validateMonthAndYear(month, year);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Authenticated user not found"
                        )
                );

        YearMonth yearMonth =
                YearMonth.of(year, month);

        LocalDate startDate =
                yearMonth.atDay(1);

        LocalDate endDate =
                yearMonth.atEndOfMonth();

        List<Income> incomes =
                incomeRepository
                        .findByUserAndIncomeDateBetweenOrderByIncomeDateDesc(
                                user,
                                startDate,
                                endDate
                        );

        List<Expense> expenses =
                expenseRepository
                        .findByUserAndExpenseDateBetweenOrderByExpenseDateDesc(
                                user,
                                startDate,
                                endDate
                        );

        BigDecimal totalIncome =
                incomes.stream()
                        .map(Income::getAmount)
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        BigDecimal totalExpenses =
                expenses.stream()
                        .map(Expense::getAmount)
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        BigDecimal balance =
                totalIncome.subtract(totalExpenses);

        Budget budget =
                budgetRepository
                        .findByUserAndMonthAndYear(
                                user,
                                month,
                                year
                        )
                        .orElse(null);

        BigDecimal budgetAmount = BigDecimal.ZERO;
        BigDecimal remainingBudget = BigDecimal.ZERO;
        BigDecimal budgetUsagePercentage = BigDecimal.ZERO;
        String budgetStatus = "NOT_SET";

        if (budget != null) {

            budgetAmount = budget.getAmount();

            remainingBudget =
                    budgetAmount.subtract(totalExpenses);

            budgetUsagePercentage =
                    totalExpenses
                            .multiply(
                                    BigDecimal.valueOf(100)
                            )
                            .divide(
                                    budgetAmount,
                                    2,
                                    RoundingMode.HALF_UP
                            );

            budgetStatus =
                    calculateBudgetStatus(
                            budgetUsagePercentage
                    );
        }

        List<TransactionResponse> recentTransactions =
                buildRecentTransactions(
                        incomes,
                        expenses
                );

        List<CategorySpendingResponse> categorySpending =
                buildCategorySpending(expenses);

        return new DashboardResponse(
                month,
                year,
                totalIncome,
                totalExpenses,
                balance,
                budgetAmount,
                remainingBudget,
                budgetUsagePercentage,
                budgetStatus,
                recentTransactions,
                categorySpending
        );
    }

    private List<TransactionResponse>
    buildRecentTransactions(
            List<Income> incomes,
            List<Expense> expenses) {

        List<TransactionResponse> transactions =
                new ArrayList<>();

        for (Income income : incomes) {

            transactions.add(
                    new TransactionResponse(
                            income.getId(),
                            "INCOME",
                            income.getSource(),
                            income.getAmount(),
                            null,
                            income.getDescription(),
                            income.getIncomeDate(),
                            income.getCreatedAt()
                    )
            );
        }

        for (Expense expense : expenses) {

            transactions.add(
                    new TransactionResponse(
                            expense.getId(),
                            "EXPENSE",
                            expense.getTitle(),
                            expense.getAmount(),
                            expense.getCategory().getName(),
                            expense.getDescription(),
                            expense.getExpenseDate(),
                            expense.getCreatedAt()
                    )
            );
        }

        transactions.sort(
                Comparator
                        .comparing(
                                TransactionResponse::transactionDate
                        )
                        .thenComparing(
                                TransactionResponse::createdAt
                        )
                        .reversed()
        );

        return transactions.stream()
                .limit(5)
                .toList();
    }

    private List<CategorySpendingResponse>
    buildCategorySpending(
            List<Expense> expenses) {

        Map<Long, CategorySpendingResponse> categoryMap =
                new LinkedHashMap<>();

        for (Expense expense : expenses) {

            Long categoryId =
                    expense.getCategory().getId();

            String categoryName =
                    expense.getCategory().getName();

            BigDecimal amount =
                    expense.getAmount();

            CategorySpendingResponse existing =
                    categoryMap.get(categoryId);

            if (existing == null) {

                categoryMap.put(
                        categoryId,
                        new CategorySpendingResponse(
                                categoryId,
                                categoryName,
                                amount
                        )
                );

            } else {

                categoryMap.put(
                        categoryId,
                        new CategorySpendingResponse(
                                categoryId,
                                categoryName,
                                existing.amount()
                                        .add(amount)
                        )
                );
            }
        }

        return categoryMap.values()
                .stream()
                .sorted(
                        Comparator.comparing(
                                CategorySpendingResponse::amount
                        ).reversed()
                )
                .toList();
    }

    private String calculateBudgetStatus(
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

    private void validateMonthAndYear(
            Integer month,
            Integer year) {

        if (month == null || year == null) {

            throw new BadRequestException(
                    "Month and year are required"
            );
        }

        if (month < 1 || month > 12) {

            throw new BadRequestException(
                    "Month must be between 1 and 12"
            );
        }

        if (year < 2000 || year > 2100) {

            throw new BadRequestException(
                    "Enter a valid year"
            );
        }
    }
}
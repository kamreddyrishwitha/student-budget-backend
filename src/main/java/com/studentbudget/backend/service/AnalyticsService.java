package com.studentbudget.backend.service;

import com.studentbudget.backend.dto.AnalyticsResponse;
import com.studentbudget.backend.dto.CategorySpendingResponse;
import com.studentbudget.backend.dto.MonthlyTrendResponse;
import com.studentbudget.backend.entity.Budget;
import com.studentbudget.backend.entity.Expense;
import com.studentbudget.backend.entity.Income;
import com.studentbudget.backend.entity.User;
import com.studentbudget.backend.exception.ResourceNotFoundException;
import com.studentbudget.backend.repository.BudgetRepository;
import com.studentbudget.backend.repository.ExpenseRepository;
import com.studentbudget.backend.repository.IncomeRepository;
import com.studentbudget.backend.repository.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class AnalyticsService {

    private final UserRepository userRepository;
    private final IncomeRepository incomeRepository;
    private final ExpenseRepository expenseRepository;
    private final BudgetRepository budgetRepository;

    public AnalyticsService(
            UserRepository userRepository,
            IncomeRepository incomeRepository,
            ExpenseRepository expenseRepository,
            BudgetRepository budgetRepository) {

        this.userRepository = userRepository;
        this.incomeRepository = incomeRepository;
        this.expenseRepository = expenseRepository;
        this.budgetRepository = budgetRepository;
    }

    @Transactional(readOnly = true)
    public AnalyticsResponse getAnalytics(
            String email,
            Integer month,
            Integer year) {

        validateMonthAndYear(month, year);

        User user = getCurrentUser(email);

        YearMonth selectedMonth =
                YearMonth.of(year, month);

        LocalDate startDate =
                selectedMonth.atDay(1);

        LocalDate endDate =
                selectedMonth.atEndOfMonth();

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
                calculateIncomeTotal(incomes);

        BigDecimal totalExpenses =
                calculateExpenseTotal(expenses);

        BigDecimal savings =
                totalIncome.subtract(totalExpenses);

        BigDecimal savingsRate =
                calculateSavingsRate(
                        totalIncome,
                        savings
                );

        Budget budget =
                budgetRepository
                        .findByUserAndMonthAndYear(
                                user,
                                month,
                                year
                        )
                        .orElse(null);

        BigDecimal budgetAmount =
                budget == null
                        ? BigDecimal.ZERO
                        : budget.getAmount();

        BigDecimal remainingBudget =
                budgetAmount.subtract(
                        totalExpenses
                );

        BigDecimal budgetUsagePercentage =
                calculateBudgetUsage(
                        budgetAmount,
                        totalExpenses
                );

        String budgetStatus =
                calculateBudgetStatus(
                        budget,
                        budgetUsagePercentage
                );

        List<CategorySpendingResponse>
                categorySpending =
                calculateCategorySpending(
                        expenses
                );

        List<MonthlyTrendResponse>
                monthlyTrend =
                calculateMonthlyTrend(
                        user,
                        selectedMonth
                );

        return new AnalyticsResponse(
                month,
                year,
                totalIncome,
                totalExpenses,
                savings,
                savingsRate,
                budgetAmount,
                remainingBudget,
                budgetUsagePercentage,
                budgetStatus,
                categorySpending,
                monthlyTrend
        );
    }

    private List<CategorySpendingResponse>
    calculateCategorySpending(
            List<Expense> expenses) {

        Map<Long, CategoryAccumulator>
                categoryTotals =
                new LinkedHashMap<>();

        for (Expense expense : expenses) {

            Long categoryId =
                    expense.getCategory()
                            .getId();

            String categoryName =
                    expense.getCategory()
                            .getName();

            CategoryAccumulator accumulator =
                    categoryTotals.computeIfAbsent(
                            categoryId,
                            id ->
                                    new CategoryAccumulator(
                                            categoryName
                                    )
                    );

            accumulator.amount =
                    accumulator.amount.add(
                            expense.getAmount()
                    );
        }

        List<CategorySpendingResponse> result =
                new ArrayList<>();

        for (Map.Entry<Long, CategoryAccumulator> entry
                : categoryTotals.entrySet()) {

            result.add(
                    new CategorySpendingResponse(
                            entry.getKey(),
                            entry.getValue().name,
                            entry.getValue().amount
                                    .setScale(
                                            2,
                                            RoundingMode.HALF_UP
                                    )
                    )
            );
        }

        result.sort(
                (first, second) ->
                        second.amount()
                                .compareTo(
                                        first.amount()
                                )
        );

        return result;
    }

    private List<MonthlyTrendResponse>
    calculateMonthlyTrend(
            User user,
            YearMonth selectedMonth) {

        List<MonthlyTrendResponse> trend =
                new ArrayList<>();

        /*
         * Includes the selected month plus
         * the previous five months.
         */
        for (int i = 5; i >= 0; i--) {

            YearMonth trendMonth =
                    selectedMonth.minusMonths(i);

            LocalDate start =
                    trendMonth.atDay(1);

            LocalDate end =
                    trendMonth.atEndOfMonth();

            List<Income> incomes =
                    incomeRepository
                            .findByUserAndIncomeDateBetweenOrderByIncomeDateDesc(
                                    user,
                                    start,
                                    end
                            );

            List<Expense> expenses =
                    expenseRepository
                            .findByUserAndExpenseDateBetweenOrderByExpenseDateDesc(
                                    user,
                                    start,
                                    end
                            );

            BigDecimal incomeTotal =
                    calculateIncomeTotal(
                            incomes
                    );

            BigDecimal expenseTotal =
                    calculateExpenseTotal(
                            expenses
                    );

            BigDecimal savings =
                    incomeTotal.subtract(
                            expenseTotal
                    );

            String monthName =
                    Month.of(
                            trendMonth.getMonthValue()
                    ).getDisplayName(
                            TextStyle.SHORT,
                            Locale.ENGLISH
                    );

            trend.add(
                    new MonthlyTrendResponse(
                            trendMonth.getMonthValue(),
                            trendMonth.getYear(),
                            monthName,
                            incomeTotal,
                            expenseTotal,
                            savings
                    )
            );
        }

        return trend;
    }

    private BigDecimal calculateIncomeTotal(
            List<Income> incomes) {

        return incomes.stream()
                .map(Income::getAmount)
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add
                )
                .setScale(
                        2,
                        RoundingMode.HALF_UP
                );
    }

    private BigDecimal calculateExpenseTotal(
            List<Expense> expenses) {

        return expenses.stream()
                .map(Expense::getAmount)
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add
                )
                .setScale(
                        2,
                        RoundingMode.HALF_UP
                );
    }

    private BigDecimal calculateSavingsRate(
            BigDecimal income,
            BigDecimal savings) {

        if (income.compareTo(
                BigDecimal.ZERO) <= 0) {

            return BigDecimal.ZERO
                    .setScale(2);
        }

        return savings
                .multiply(
                        new BigDecimal("100")
                )
                .divide(
                        income,
                        2,
                        RoundingMode.HALF_UP
                );
    }

    private BigDecimal calculateBudgetUsage(
            BigDecimal budgetAmount,
            BigDecimal expenses) {

        if (budgetAmount.compareTo(
                BigDecimal.ZERO) <= 0) {

            return BigDecimal.ZERO
                    .setScale(2);
        }

        return expenses
                .multiply(
                        new BigDecimal("100")
                )
                .divide(
                        budgetAmount,
                        2,
                        RoundingMode.HALF_UP
                );
    }

    private String calculateBudgetStatus(
            Budget budget,
            BigDecimal usagePercentage) {

        if (budget == null) {
            return "NOT_SET";
        }

        if (usagePercentage.compareTo(
                new BigDecimal("100")) >= 0) {

            return "EXCEEDED";
        }

        if (usagePercentage.compareTo(
                new BigDecimal("90")) >= 0) {

            return "NEAR_LIMIT";
        }

        if (usagePercentage.compareTo(
                new BigDecimal("75")) >= 0) {

            return "WARNING";
        }

        return "SAFE";
    }

    private User getCurrentUser(
            String email) {

        return userRepository
                .findByEmail(
                        email.trim()
                                .toLowerCase()
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Authenticated user not found"
                        )
                );
    }

    private void validateMonthAndYear(
            Integer month,
            Integer year) {

        if (month == null
                || month < 1
                || month > 12) {

            throw new IllegalArgumentException(
                    "Month must be between 1 and 12"
            );
        }

        if (year == null
                || year < 2000
                || year > 2100) {

            throw new IllegalArgumentException(
                    "Year must be between 2000 and 2100"
            );
        }
    }

    private static class CategoryAccumulator {

        private final String name;
        private BigDecimal amount =
                BigDecimal.ZERO;

        private CategoryAccumulator(
                String name) {

            this.name = name;
        }
    }
}
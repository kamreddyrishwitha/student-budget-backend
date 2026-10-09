package com.studentbudget.backend.service;

import com.studentbudget.backend.dto.TransactionResponse;
import com.studentbudget.backend.entity.Expense;
import com.studentbudget.backend.entity.Income;
import com.studentbudget.backend.entity.User;
import com.studentbudget.backend.exception.BadRequestException;
import com.studentbudget.backend.exception.ResourceNotFoundException;
import com.studentbudget.backend.repository.ExpenseRepository;
import com.studentbudget.backend.repository.IncomeRepository;
import com.studentbudget.backend.repository.UserRepository;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class TransactionService {

    private final IncomeRepository incomeRepository;
    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;

    public TransactionService(
            IncomeRepository incomeRepository,
            ExpenseRepository expenseRepository,
            UserRepository userRepository) {

        this.incomeRepository = incomeRepository;
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

    public List<TransactionResponse> getTransactions(
            String email,
            String type,
            Integer month,
            Integer year,
            Long categoryId) {

        User user = getCurrentUser(email);

        String normalizedType =
                type == null
                        ? "ALL"
                        : type.trim().toUpperCase();

        if (!normalizedType.equals("ALL")
                && !normalizedType.equals("INCOME")
                && !normalizedType.equals("EXPENSE")) {

            throw new BadRequestException(
                    "Type must be ALL, INCOME or EXPENSE"
            );
        }

        if ((month == null && year != null)
                || (month != null && year == null)) {

            throw new BadRequestException(
                    "Month and year must be provided together"
            );
        }

        if (month != null
                && (month < 1 || month > 12)) {

            throw new BadRequestException(
                    "Month must be between 1 and 12"
            );
        }

        if (categoryId != null
                && normalizedType.equals("INCOME")) {

            throw new BadRequestException(
                    "Category filter can only be used for expenses"
            );
        }

        LocalDate startDate = null;
        LocalDate endDate = null;

        if (month != null) {

            YearMonth yearMonth =
                    YearMonth.of(
                            year,
                            month
                    );

            startDate = yearMonth.atDay(1);
            endDate = yearMonth.atEndOfMonth();
        }

        List<TransactionResponse> transactions =
                new ArrayList<>();

        if (normalizedType.equals("ALL")
                || normalizedType.equals("INCOME")) {

            List<Income> incomes;

            if (startDate != null) {

                incomes =
                        incomeRepository
                                .findByUserAndIncomeDateBetweenOrderByIncomeDateDesc(
                                        user,
                                        startDate,
                                        endDate
                                );

            } else {

                incomes =
                        incomeRepository
                                .findByUserOrderByIncomeDateDesc(
                                        user
                                );
            }

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
        }

        if (normalizedType.equals("ALL")
                || normalizedType.equals("EXPENSE")) {

            List<Expense> expenses;

            if (categoryId != null
                    && startDate != null) {

                expenses =
                        expenseRepository
                                .findByUserAndCategoryIdAndExpenseDateBetweenOrderByExpenseDateDesc(
                                        user,
                                        categoryId,
                                        startDate,
                                        endDate
                                );

            } else if (categoryId != null) {

                expenses =
                        expenseRepository
                                .findByUserAndCategoryIdOrderByExpenseDateDesc(
                                        user,
                                        categoryId
                                );

            } else if (startDate != null) {

                expenses =
                        expenseRepository
                                .findByUserAndExpenseDateBetweenOrderByExpenseDateDesc(
                                        user,
                                        startDate,
                                        endDate
                                );

            } else {

                expenses =
                        expenseRepository
                                .findByUserOrderByExpenseDateDesc(
                                        user
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

        return transactions;
    }
}
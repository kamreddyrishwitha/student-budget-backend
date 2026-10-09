package com.studentbudget.backend.repository;

import com.studentbudget.backend.entity.Expense;
import com.studentbudget.backend.entity.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExpenseRepository
        extends JpaRepository<Expense, Long> {

    List<Expense> findByUserOrderByExpenseDateDesc(
            User user
    );

    Optional<Expense> findByIdAndUser(
            Long id,
            User user
    );

    List<Expense> findByUserAndExpenseDateBetween(
            User user,
            LocalDate startDate,
            LocalDate endDate
    );

    List<Expense>
    findByUserAndExpenseDateBetweenOrderByExpenseDateDesc(
            User user,
            LocalDate startDate,
            LocalDate endDate
    );

    List<Expense>
    findByUserAndCategoryIdOrderByExpenseDateDesc(
            User user,
            Long categoryId
    );

    List<Expense>
    findByUserAndCategoryIdAndExpenseDateBetweenOrderByExpenseDateDesc(
            User user,
            Long categoryId,
            LocalDate startDate,
            LocalDate endDate
    );
}
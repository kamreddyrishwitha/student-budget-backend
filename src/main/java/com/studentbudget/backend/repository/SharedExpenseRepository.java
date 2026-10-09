package com.studentbudget.backend.repository;

import com.studentbudget.backend.entity.ExpenseGroup;
import com.studentbudget.backend.entity.SharedExpense;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SharedExpenseRepository
        extends JpaRepository<SharedExpense, Long> {

    List<SharedExpense>
    findByGroupOrderByExpenseDateDescCreatedAtDesc(
            ExpenseGroup group
    );

    Optional<SharedExpense>
    findByIdAndGroup(
            Long id,
            ExpenseGroup group
    );
}
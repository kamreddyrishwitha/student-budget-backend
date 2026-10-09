package com.studentbudget.backend.repository;

import com.studentbudget.backend.entity.Budget;
import com.studentbudget.backend.entity.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BudgetRepository
        extends JpaRepository<Budget, Long> {

    Optional<Budget> findByUserAndMonthAndYear(
            User user,
            Integer month,
            Integer year
    );

    Optional<Budget> findByIdAndUser(
            Long id,
            User user
    );

    List<Budget> findByUserOrderByYearDescMonthDesc(
            User user
    );
}
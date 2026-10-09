package com.studentbudget.backend.repository;

import com.studentbudget.backend.entity.ExpenseSplit;
import com.studentbudget.backend.entity.SharedExpense;
import com.studentbudget.backend.entity.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExpenseSplitRepository
        extends JpaRepository<ExpenseSplit, Long> {

    List<ExpenseSplit>
    findBySharedExpenseOrderByIdAsc(
            SharedExpense sharedExpense
    );

    Optional<ExpenseSplit>
    findBySharedExpenseAndUser(
            SharedExpense sharedExpense,
            User user
    );

    List<ExpenseSplit>
    findByUser(
            User user
    );
}
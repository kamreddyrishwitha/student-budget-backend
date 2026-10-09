package com.studentbudget.backend.repository;

import com.studentbudget.backend.entity.ExpenseGroup;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ExpenseGroupRepository
        extends JpaRepository<ExpenseGroup, Long> {

    Optional<ExpenseGroup> findByIdAndCreatedByEmail(
            Long id,
            String email
    );
}
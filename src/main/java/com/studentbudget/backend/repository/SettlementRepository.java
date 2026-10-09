package com.studentbudget.backend.repository;

import com.studentbudget.backend.entity.ExpenseGroup;
import com.studentbudget.backend.entity.Settlement;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SettlementRepository
        extends JpaRepository<Settlement, Long> {

    List<Settlement>
    findByGroupOrderBySettlementDateDescCreatedAtDesc(
            ExpenseGroup group
    );
}
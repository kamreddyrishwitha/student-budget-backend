package com.studentbudget.backend.repository;

import com.studentbudget.backend.entity.Income;
import com.studentbudget.backend.entity.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface IncomeRepository
        extends JpaRepository<Income, Long> {

    List<Income> findByUserOrderByIncomeDateDesc(
            User user
    );

    Optional<Income> findByIdAndUser(
            Long id,
            User user
    );

    List<Income>
    findByUserAndIncomeDateBetweenOrderByIncomeDateDesc(
            User user,
            LocalDate startDate,
            LocalDate endDate
    );
}
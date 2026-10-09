package com.studentbudget.backend.service;

import com.studentbudget.backend.dto.IncomeRequest;
import com.studentbudget.backend.entity.Income;
import com.studentbudget.backend.entity.User;
import com.studentbudget.backend.repository.IncomeRepository;
import com.studentbudget.backend.repository.UserRepository;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class IncomeService {

    private final IncomeRepository incomeRepository;
    private final UserRepository userRepository;

    public IncomeService(
            IncomeRepository incomeRepository,
            UserRepository userRepository) {

        this.incomeRepository = incomeRepository;
        this.userRepository = userRepository;
    }

    private User getCurrentUser(String email) {

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Authenticated user not found"
                        )
                );
    }

    public Income addIncome(
            String email,
            IncomeRequest request) {

        User user = getCurrentUser(email);

        Income income = Income.builder()
                .amount(request.amount())
                .source(request.source().trim())
                .description(
                        cleanDescription(
                                request.description()
                        )
                )
                .incomeDate(request.incomeDate())
                .user(user)
                .build();

        return incomeRepository.save(income);
    }

    public List<Income> getAllIncomes(
            String email) {

        User user = getCurrentUser(email);

        return incomeRepository
                .findByUserOrderByIncomeDateDesc(user);
    }

    public Income getIncomeById(
            String email,
            Long id) {

        User user = getCurrentUser(email);

        return incomeRepository
                .findByIdAndUser(id, user)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Income record not found"
                        )
                );
    }

    public Income updateIncome(
            String email,
            Long id,
            IncomeRequest request) {

        User user = getCurrentUser(email);

        Income income = incomeRepository
                .findByIdAndUser(id, user)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Income record not found"
                        )
                );

        income.setAmount(request.amount());
        income.setSource(
                request.source().trim()
        );
        income.setDescription(
                cleanDescription(
                        request.description()
                )
        );
        income.setIncomeDate(
                request.incomeDate()
        );

        return incomeRepository.save(income);
    }

    public void deleteIncome(
            String email,
            Long id) {

        User user = getCurrentUser(email);

        Income income = incomeRepository
                .findByIdAndUser(id, user)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Income record not found"
                        )
                );

        incomeRepository.delete(income);
    }

    private String cleanDescription(
            String description) {

        if (description == null) {
            return null;
        }

        String cleaned = description.trim();

        return cleaned.isEmpty()
                ? null
                : cleaned;
    }
}
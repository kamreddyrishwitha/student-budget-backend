package com.studentbudget.backend.service;

import com.studentbudget.backend.dto.*;
import com.studentbudget.backend.entity.*;
import com.studentbudget.backend.exception.BadRequestException;
import com.studentbudget.backend.exception.ResourceNotFoundException;
import com.studentbudget.backend.repository.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class SharedExpenseService {

    private final SharedExpenseRepository sharedExpenseRepository;
    private final ExpenseSplitRepository expenseSplitRepository;
    private final ExpenseGroupRepository expenseGroupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UserRepository userRepository;

    public SharedExpenseService(
            SharedExpenseRepository sharedExpenseRepository,
            ExpenseSplitRepository expenseSplitRepository,
            ExpenseGroupRepository expenseGroupRepository,
            GroupMemberRepository groupMemberRepository,
            UserRepository userRepository) {

        this.sharedExpenseRepository = sharedExpenseRepository;
        this.expenseSplitRepository = expenseSplitRepository;
        this.expenseGroupRepository = expenseGroupRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public SharedExpenseResponse createEqualSplitExpense(
            String email,
            Long groupId,
            SharedExpenseRequest request) {

        User currentUser = getCurrentUser(email);
        ExpenseGroup group = getGroup(groupId);

        verifyMembership(group, currentUser);

        User paidBy = getUser(request.paidByUserId());
        verifyMembership(group, paidBy);

        List<GroupMember> members =
                groupMemberRepository
                        .findByGroupOrderByJoinedAtAsc(group);

        if (members.isEmpty()) {
            throw new BadRequestException(
                    "Group has no members"
            );
        }

        SharedExpense expense =
                createExpense(
                        group,
                        paidBy,
                        request.title(),
                        request.amount(),
                        request.description(),
                        request.expenseDate(),
                        SplitType.EQUAL
                );

        createEqualSplits(
                expense,
                members
        );

        return buildResponse(expense);
    }

    @Transactional
    public SharedExpenseResponse createCustomSplitExpense(
            String email,
            Long groupId,
            CustomSplitRequest request) {

        User currentUser = getCurrentUser(email);
        ExpenseGroup group = getGroup(groupId);

        verifyMembership(group, currentUser);

        User paidBy = getUser(request.paidByUserId());
        verifyMembership(group, paidBy);

        validateParticipants(
                group,
                request.participants()
        );

        BigDecimal splitTotal =
                request.participants()
                        .stream()
                        .map(SplitParticipantRequest::value)
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        if (splitTotal.compareTo(request.amount()) != 0) {
            throw new BadRequestException(
                    "Custom split amounts must equal the total expense amount"
            );
        }

        SharedExpense expense =
                createExpense(
                        group,
                        paidBy,
                        request.title(),
                        request.amount(),
                        request.description(),
                        request.expenseDate(),
                        SplitType.CUSTOM
                );

        List<ExpenseSplit> splits =
                new ArrayList<>();

        for (SplitParticipantRequest participant
                : request.participants()) {

            User user =
                    getUser(
                            participant.userId()
                    );

            ExpenseSplit split =
                    ExpenseSplit.builder()
                            .sharedExpense(expense)
                            .user(user)
                            .shareAmount(
                                    participant
                                            .value()
                                            .setScale(
                                                    2,
                                                    RoundingMode.HALF_UP
                                            )
                            )
                            .settledAmount(
                                    BigDecimal.ZERO
                            )
                            .build();

            splits.add(split);
        }

        expenseSplitRepository.saveAll(splits);

        return buildResponse(expense);
    }

    @Transactional
    public SharedExpenseResponse createPercentageSplitExpense(
            String email,
            Long groupId,
            PercentageSplitRequest request) {

        User currentUser = getCurrentUser(email);
        ExpenseGroup group = getGroup(groupId);

        verifyMembership(group, currentUser);

        User paidBy = getUser(request.paidByUserId());
        verifyMembership(group, paidBy);

        validateParticipants(
                group,
                request.participants()
        );

        BigDecimal percentageTotal =
                request.participants()
                        .stream()
                        .map(SplitParticipantRequest::value)
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        if (percentageTotal.compareTo(
                new BigDecimal("100")) != 0) {

            throw new BadRequestException(
                    "Percentage split must total exactly 100%"
            );
        }

        SharedExpense expense =
                createExpense(
                        group,
                        paidBy,
                        request.title(),
                        request.amount(),
                        request.description(),
                        request.expenseDate(),
                        SplitType.PERCENTAGE
                );

        List<ExpenseSplit> splits =
                new ArrayList<>();

        BigDecimal assignedAmount =
                BigDecimal.ZERO;

        for (int i = 0;
             i < request.participants().size();
             i++) {

            SplitParticipantRequest participant =
                    request.participants().get(i);

            User user =
                    getUser(
                            participant.userId()
                    );

            BigDecimal share;

            if (i ==
                    request.participants().size() - 1) {

                share =
                        request.amount()
                                .subtract(
                                        assignedAmount
                                )
                                .setScale(
                                        2,
                                        RoundingMode.HALF_UP
                                );

            } else {

                share =
                        request.amount()
                                .multiply(
                                        participant.value()
                                )
                                .divide(
                                        new BigDecimal("100"),
                                        2,
                                        RoundingMode.HALF_UP
                                );

                assignedAmount =
                        assignedAmount.add(share);
            }

            ExpenseSplit split =
                    ExpenseSplit.builder()
                            .sharedExpense(expense)
                            .user(user)
                            .shareAmount(share)
                            .settledAmount(
                                    BigDecimal.ZERO
                            )
                            .build();

            splits.add(split);
        }

        expenseSplitRepository.saveAll(splits);

        return buildResponse(expense);
    }

    @Transactional(readOnly = true)
    public List<SharedExpenseResponse> getGroupExpenses(
            String email,
            Long groupId) {

        User currentUser = getCurrentUser(email);
        ExpenseGroup group = getGroup(groupId);

        verifyMembership(group, currentUser);

        return sharedExpenseRepository
                .findByGroupOrderByExpenseDateDescCreatedAtDesc(
                        group
                )
                .stream()
                .map(this::buildResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public SharedExpenseResponse getExpenseById(
            String email,
            Long groupId,
            Long expenseId) {

        User currentUser = getCurrentUser(email);
        ExpenseGroup group = getGroup(groupId);

        verifyMembership(group, currentUser);

        SharedExpense expense =
                sharedExpenseRepository
                        .findByIdAndGroup(
                                expenseId,
                                group
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Shared expense not found"
                                )
                        );

        return buildResponse(expense);
    }

    private SharedExpense createExpense(
            ExpenseGroup group,
            User paidBy,
            String title,
            BigDecimal amount,
            String description,
            java.time.LocalDate expenseDate,
            SplitType splitType) {

        SharedExpense expense =
                SharedExpense.builder()
                        .title(title.trim())
                        .amount(
                                amount.setScale(
                                        2,
                                        RoundingMode.HALF_UP
                                )
                        )
                        .description(
                                cleanDescription(
                                        description
                                )
                        )
                        .expenseDate(expenseDate)
                        .splitType(splitType)
                        .group(group)
                        .paidBy(paidBy)
                        .build();

        return sharedExpenseRepository
                .save(expense);
    }

    private void createEqualSplits(
            SharedExpense expense,
            List<GroupMember> members) {

        int memberCount =
                members.size();

        BigDecimal baseShare =
                expense.getAmount()
                        .divide(
                                BigDecimal.valueOf(
                                        memberCount
                                ),
                                2,
                                RoundingMode.DOWN
                        );

        BigDecimal distributed =
                baseShare.multiply(
                        BigDecimal.valueOf(
                                memberCount
                        )
                );

        BigDecimal remainder =
                expense.getAmount()
                        .subtract(distributed);

        List<ExpenseSplit> splits =
                new ArrayList<>();

        for (int i = 0;
             i < members.size();
             i++) {

            BigDecimal share =
                    baseShare;

            if (i == 0) {
                share =
                        share.add(
                                remainder
                        );
            }

            ExpenseSplit split =
                    ExpenseSplit.builder()
                            .sharedExpense(expense)
                            .user(
                                    members
                                            .get(i)
                                            .getUser()
                            )
                            .shareAmount(share)
                            .settledAmount(
                                    BigDecimal.ZERO
                            )
                            .build();

            splits.add(split);
        }

        expenseSplitRepository.saveAll(splits);
    }

    private void validateParticipants(
            ExpenseGroup group,
            List<SplitParticipantRequest> participants) {

        Set<Long> userIds =
                new HashSet<>();

        for (SplitParticipantRequest participant
                : participants) {

            if (!userIds.add(
                    participant.userId())) {

                throw new BadRequestException(
                        "A user cannot appear more than once in a split"
                );
            }

            User user =
                    getUser(
                            participant.userId()
                    );

            verifyMembership(
                    group,
                    user
            );
        }
    }

    private SharedExpenseResponse buildResponse(
            SharedExpense expense) {

        List<ExpenseSplitResponse> splits =
                expenseSplitRepository
                        .findBySharedExpenseOrderByIdAsc(
                                expense
                        )
                        .stream()
                        .map(split -> {

                            BigDecimal pending =
                                    split.getShareAmount()
                                            .subtract(
                                                    split.getSettledAmount()
                                            );

                            return new ExpenseSplitResponse(
                                    split.getUser().getId(),
                                    split.getUser().getName(),
                                    split.getUser().getEmail(),
                                    split.getShareAmount(),
                                    split.getSettledAmount(),
                                    pending
                            );
                        })
                        .toList();

        return new SharedExpenseResponse(
                expense.getId(),
                expense.getGroup().getId(),
                expense.getGroup().getName(),
                expense.getTitle(),
                expense.getAmount(),
                expense.getDescription(),
                expense.getExpenseDate(),
                expense.getSplitType().name(),
                expense.getPaidBy().getId(),
                expense.getPaidBy().getName(),
                expense.getCreatedAt(),
                splits
        );
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

    private User getUser(
            Long userId) {

        return userRepository
                .findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found"
                        )
                );
    }

    private ExpenseGroup getGroup(
            Long groupId) {

        return expenseGroupRepository
                .findById(groupId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Group not found"
                        )
                );
    }

    private void verifyMembership(
            ExpenseGroup group,
            User user) {

        if (!groupMemberRepository
                .existsByGroupAndUser(
                        group,
                        user
                )) {

            throw new ResourceNotFoundException(
                    "Group not found"
            );
        }
    }

    private String cleanDescription(
            String description) {

        if (description == null) {
            return null;
        }

        String cleaned =
                description.trim();

        return cleaned.isEmpty()
                ? null
                : cleaned;
    }
}
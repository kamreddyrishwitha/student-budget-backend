package com.studentbudget.backend.service;

import com.studentbudget.backend.dto.DebtResponse;
import com.studentbudget.backend.dto.GroupBalanceResponse;
import com.studentbudget.backend.entity.ExpenseGroup;
import com.studentbudget.backend.entity.ExpenseSplit;
import com.studentbudget.backend.entity.GroupMember;
import com.studentbudget.backend.entity.Settlement;
import com.studentbudget.backend.entity.SharedExpense;
import com.studentbudget.backend.entity.User;
import com.studentbudget.backend.exception.ResourceNotFoundException;
import com.studentbudget.backend.repository.ExpenseGroupRepository;
import com.studentbudget.backend.repository.ExpenseSplitRepository;
import com.studentbudget.backend.repository.GroupMemberRepository;
import com.studentbudget.backend.repository.SettlementRepository;
import com.studentbudget.backend.repository.SharedExpenseRepository;
import com.studentbudget.backend.repository.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GroupBalanceService {

    private final ExpenseGroupRepository expenseGroupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final SharedExpenseRepository sharedExpenseRepository;
    private final ExpenseSplitRepository expenseSplitRepository;
    private final SettlementRepository settlementRepository;
    private final UserRepository userRepository;

    public GroupBalanceService(
            ExpenseGroupRepository expenseGroupRepository,
            GroupMemberRepository groupMemberRepository,
            SharedExpenseRepository sharedExpenseRepository,
            ExpenseSplitRepository expenseSplitRepository,
            SettlementRepository settlementRepository,
            UserRepository userRepository) {

        this.expenseGroupRepository =
                expenseGroupRepository;

        this.groupMemberRepository =
                groupMemberRepository;

        this.sharedExpenseRepository =
                sharedExpenseRepository;

        this.expenseSplitRepository =
                expenseSplitRepository;

        this.settlementRepository =
                settlementRepository;

        this.userRepository =
                userRepository;
    }

    @Transactional(readOnly = true)
    public GroupBalanceResponse getGroupBalances(
            String email,
            Long groupId) {

        User currentUser =
                getCurrentUser(email);

        ExpenseGroup group =
                expenseGroupRepository
                        .findById(groupId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Group not found"
                                )
                        );

        verifyMembership(
                group,
                currentUser
        );

        List<GroupMember> members =
                groupMemberRepository
                        .findByGroupOrderByJoinedAtAsc(
                                group
                        );

        Map<Long, User> users =
                new HashMap<>();

        Map<Long, BigDecimal> balances =
                new HashMap<>();

        for (GroupMember member : members) {

            User user =
                    member.getUser();

            users.put(
                    user.getId(),
                    user
            );

            balances.put(
                    user.getId(),
                    BigDecimal.ZERO
            );
        }

        /*
         * EXPENSE CALCULATION
         *
         * Payer receives credit for the full amount.
         * Every participant is charged their share.
         */
        List<SharedExpense> expenses =
                sharedExpenseRepository
                        .findByGroupOrderByExpenseDateDescCreatedAtDesc(
                                group
                        );

        for (SharedExpense expense : expenses) {

            Long payerId =
                    expense.getPaidBy().getId();

            balances.put(
                    payerId,
                    balances.get(payerId)
                            .add(
                                    expense.getAmount()
                            )
            );

            List<ExpenseSplit> splits =
                    expenseSplitRepository
                            .findBySharedExpenseOrderByIdAsc(
                                    expense
                            );

            for (ExpenseSplit split : splits) {

                Long userId =
                        split.getUser().getId();

                balances.put(
                        userId,
                        balances.get(userId)
                                .subtract(
                                        split.getShareAmount()
                                )
                );
            }
        }

        /*
         * SETTLEMENT CALCULATION
         *
         * Example:
         * Rahul owes Test User ₹1800.
         * Rahul pays Test User ₹500.
         *
         * Rahul's balance increases by ₹500.
         * Test User's balance decreases by ₹500.
         */
        List<Settlement> settlements =
                settlementRepository
                        .findByGroupOrderBySettlementDateDescCreatedAtDesc(
                                group
                        );

        for (Settlement settlement : settlements) {

            Long fromUserId =
                    settlement.getFromUser().getId();

            Long toUserId =
                    settlement.getToUser().getId();

            BigDecimal amount =
                    settlement.getAmount();

            balances.put(
                    fromUserId,
                    balances.get(fromUserId)
                            .add(amount)
            );

            balances.put(
                    toUserId,
                    balances.get(toUserId)
                            .subtract(amount)
            );
        }

        List<BalanceEntry> creditors =
                new ArrayList<>();

        List<BalanceEntry> debtors =
                new ArrayList<>();

        for (Map.Entry<Long, BigDecimal> entry
                : balances.entrySet()) {

            BigDecimal balance =
                    entry.getValue()
                            .setScale(
                                    2,
                                    RoundingMode.HALF_UP
                            );

            if (balance.compareTo(
                    BigDecimal.ZERO) > 0) {

                creditors.add(
                        new BalanceEntry(
                                entry.getKey(),
                                balance
                        )
                );

            } else if (balance.compareTo(
                    BigDecimal.ZERO) < 0) {

                debtors.add(
                        new BalanceEntry(
                                entry.getKey(),
                                balance.abs()
                        )
                );
            }
        }

        creditors.sort(
                Comparator.comparing(
                        BalanceEntry::amount
                ).reversed()
        );

        debtors.sort(
                Comparator.comparing(
                        BalanceEntry::amount
                ).reversed()
        );

        List<DebtResponse> debts =
                calculateDebts(
                        creditors,
                        debtors,
                        users
                );

        return new GroupBalanceResponse(
                group.getId(),
                group.getName(),
                debts
        );
    }

    private List<DebtResponse> calculateDebts(
            List<BalanceEntry> creditors,
            List<BalanceEntry> debtors,
            Map<Long, User> users) {

        List<DebtResponse> debts =
                new ArrayList<>();

        int debtorIndex = 0;
        int creditorIndex = 0;

        while (debtorIndex < debtors.size()
                && creditorIndex < creditors.size()) {

            BalanceEntry debtor =
                    debtors.get(debtorIndex);

            BalanceEntry creditor =
                    creditors.get(creditorIndex);

            BigDecimal payment =
                    debtor.amount()
                            .min(
                                    creditor.amount()
                            )
                            .setScale(
                                    2,
                                    RoundingMode.HALF_UP
                            );

            if (payment.compareTo(
                    BigDecimal.ZERO) > 0) {

                User fromUser =
                        users.get(
                                debtor.userId()
                        );

                User toUser =
                        users.get(
                                creditor.userId()
                        );

                debts.add(
                        new DebtResponse(
                                fromUser.getId(),
                                fromUser.getName(),
                                toUser.getId(),
                                toUser.getName(),
                                payment
                        )
                );
            }

            BigDecimal debtorRemaining =
                    debtor.amount()
                            .subtract(payment);

            BigDecimal creditorRemaining =
                    creditor.amount()
                            .subtract(payment);

            debtors.set(
                    debtorIndex,
                    new BalanceEntry(
                            debtor.userId(),
                            debtorRemaining
                    )
            );

            creditors.set(
                    creditorIndex,
                    new BalanceEntry(
                            creditor.userId(),
                            creditorRemaining
                    )
            );

            if (debtorRemaining.compareTo(
                    BigDecimal.ZERO) == 0) {

                debtorIndex++;
            }

            if (creditorRemaining.compareTo(
                    BigDecimal.ZERO) == 0) {

                creditorIndex++;
            }
        }

        return debts;
    }

    private User getCurrentUser(
            String email) {

        return userRepository
                .findByEmail(
                        email.trim().toLowerCase()
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Authenticated user not found"
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

    private record BalanceEntry(
            Long userId,
            BigDecimal amount
    ) {
    }
}
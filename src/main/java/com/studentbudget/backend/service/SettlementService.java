package com.studentbudget.backend.service;

import com.studentbudget.backend.dto.DebtResponse;
import com.studentbudget.backend.dto.GroupBalanceResponse;
import com.studentbudget.backend.dto.SettlementRequest;
import com.studentbudget.backend.dto.SettlementResponse;

import com.studentbudget.backend.entity.ExpenseGroup;
import com.studentbudget.backend.entity.Settlement;
import com.studentbudget.backend.entity.User;

import com.studentbudget.backend.exception.BadRequestException;
import com.studentbudget.backend.exception.ResourceNotFoundException;

import com.studentbudget.backend.repository.ExpenseGroupRepository;
import com.studentbudget.backend.repository.GroupMemberRepository;
import com.studentbudget.backend.repository.SettlementRepository;
import com.studentbudget.backend.repository.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class SettlementService {

    private final SettlementRepository settlementRepository;
    private final ExpenseGroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UserRepository userRepository;
    private final GroupBalanceService groupBalanceService;

    public SettlementService(
            SettlementRepository settlementRepository,
            ExpenseGroupRepository groupRepository,
            GroupMemberRepository groupMemberRepository,
            UserRepository userRepository,
            GroupBalanceService groupBalanceService) {

        this.settlementRepository = settlementRepository;
        this.groupRepository = groupRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.userRepository = userRepository;
        this.groupBalanceService = groupBalanceService;
    }

    @Transactional
    public SettlementResponse createSettlement(
            String email,
            Long groupId,
            SettlementRequest request) {

        User currentUser = getCurrentUser(email);
        ExpenseGroup group = getGroup(groupId);

        validateMembership(group, currentUser);

        User fromUser = getUser(request.fromUserId());
        User toUser = getUser(request.toUserId());

        validateMembership(group, fromUser);
        validateMembership(group, toUser);

        if (fromUser.getId().equals(toUser.getId())) {
            throw new BadRequestException(
                    "Cannot settle an expense with yourself"
            );
        }

        if (!currentUser.getId().equals(fromUser.getId())) {
            throw new BadRequestException(
                    "You can only record your own payments"
            );
        }

        BigDecimal amount = request.amount()
                .setScale(2, RoundingMode.UNNECESSARY);

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException(
                    "Settlement amount must be greater than zero"
            );
        }

        GroupBalanceResponse balances =
                groupBalanceService.getGroupBalances(
                        email,
                        groupId
                );

        DebtResponse outstandingDebt = balances.debts()
                .stream()
                .filter(debt ->
                        debt.fromUserId().equals(fromUser.getId())
                                && debt.toUserId().equals(toUser.getId())
                )
                .findFirst()
                .orElseThrow(() ->
                        new BadRequestException(
                                "No outstanding debt exists between these users"
                        )
                );

        if (amount.compareTo(outstandingDebt.amount()) > 0) {
            throw new BadRequestException(
                    "Settlement amount cannot exceed outstanding debt of "
                            + outstandingDebt.amount()
            );
        }

        Settlement settlement = Settlement.builder()
                .group(group)
                .fromUser(fromUser)
                .toUser(toUser)
                .amount(amount)
                .settlementDate(request.settlementDate())
                .note(request.note())
                .build();

        Settlement saved =
                settlementRepository.save(settlement);

        return buildResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<SettlementResponse> getGroupSettlements(
            String email,
            Long groupId) {

        User currentUser = getCurrentUser(email);
        ExpenseGroup group = getGroup(groupId);

        validateMembership(group, currentUser);

        return settlementRepository
                .findByGroupOrderBySettlementDateDescCreatedAtDesc(group)
                .stream()
                .map(this::buildResponse)
                .toList();
    }

    private SettlementResponse buildResponse(
            Settlement settlement) {

        return new SettlementResponse(
                settlement.getId(),
                settlement.getGroup().getId(),
                settlement.getFromUser().getId(),
                settlement.getFromUser().getName(),
                settlement.getToUser().getId(),
                settlement.getToUser().getName(),
                settlement.getAmount(),
                settlement.getSettlementDate(),
                settlement.getNote(),
                settlement.getCreatedAt()
        );
    }

    private User getCurrentUser(String email) {

        return userRepository.findByEmail(
                email.trim().toLowerCase()
        ).orElseThrow(() ->
                new ResourceNotFoundException(
                        "Authenticated user not found"
                )
        );
    }

    private User getUser(Long userId) {

        return userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found"
                        )
                );
    }

    private ExpenseGroup getGroup(Long groupId) {

        return groupRepository.findById(groupId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Group not found"
                        )
                );
    }

    private void validateMembership(
            ExpenseGroup group,
            User user) {

        boolean isMember = groupMemberRepository
                .existsByGroupAndUser(group, user);

        if (!isMember) {
            throw new BadRequestException(
                    "User is not a member of this group"
            );
        }
    }
}
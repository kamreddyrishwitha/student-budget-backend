package com.studentbudget.backend.service;

import com.studentbudget.backend.dto.AddGroupMemberRequest;
import com.studentbudget.backend.dto.GroupMemberResponse;
import com.studentbudget.backend.dto.GroupRequest;
import com.studentbudget.backend.dto.GroupResponse;
import com.studentbudget.backend.entity.ExpenseGroup;
import com.studentbudget.backend.entity.GroupMember;
import com.studentbudget.backend.entity.User;
import com.studentbudget.backend.exception.BadRequestException;
import com.studentbudget.backend.exception.ResourceNotFoundException;
import com.studentbudget.backend.repository.ExpenseGroupRepository;
import com.studentbudget.backend.repository.GroupMemberRepository;
import com.studentbudget.backend.repository.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class GroupService {

    private final ExpenseGroupRepository expenseGroupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UserRepository userRepository;

    public GroupService(
            ExpenseGroupRepository expenseGroupRepository,
            GroupMemberRepository groupMemberRepository,
            UserRepository userRepository) {

        this.expenseGroupRepository = expenseGroupRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.userRepository = userRepository;
    }

    private User getCurrentUser(String email) {

        return userRepository.findByEmail(
                        email.trim().toLowerCase()
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Authenticated user not found"
                        )
                );
    }

    @Transactional
    public GroupResponse createGroup(
            String email,
            GroupRequest request) {

        User creator = getCurrentUser(email);

        ExpenseGroup group = ExpenseGroup.builder()
                .name(request.name().trim())
                .description(
                        cleanDescription(
                                request.description()
                        )
                )
                .createdBy(creator)
                .build();

        ExpenseGroup savedGroup =
                expenseGroupRepository.save(group);

        GroupMember creatorMembership =
                GroupMember.builder()
                        .group(savedGroup)
                        .user(creator)
                        .build();

        groupMemberRepository.save(
                creatorMembership
        );

        return buildGroupResponse(
                savedGroup
        );
    }

    @Transactional(readOnly = true)
    public List<GroupResponse> getMyGroups(
            String email) {

        User user = getCurrentUser(email);

        return groupMemberRepository
                .findByUserOrderByJoinedAtDesc(user)
                .stream()
                .map(GroupMember::getGroup)
                .map(this::buildGroupResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public GroupResponse getGroupById(
            String email,
            Long groupId) {

        User user = getCurrentUser(email);

        ExpenseGroup group =
                getGroup(groupId);

        verifyMembership(
                group,
                user
        );

        return buildGroupResponse(group);
    }

    @Transactional
    public GroupResponse updateGroup(
            String email,
            Long groupId,
            GroupRequest request) {

        User currentUser =
                getCurrentUser(email);

        ExpenseGroup group =
                getGroup(groupId);

        verifyCreator(
                group,
                currentUser
        );

        group.setName(
                request.name().trim()
        );

        group.setDescription(
                cleanDescription(
                        request.description()
                )
        );

        ExpenseGroup savedGroup =
                expenseGroupRepository.save(group);

        return buildGroupResponse(
                savedGroup
        );
    }

    @Transactional
    public GroupResponse addMember(
            String email,
            Long groupId,
            AddGroupMemberRequest request) {

        User currentUser =
                getCurrentUser(email);

        ExpenseGroup group =
                getGroup(groupId);

        verifyCreator(
                group,
                currentUser
        );

        String memberEmail =
                request.email()
                        .trim()
                        .toLowerCase();

        User member =
                userRepository
                        .findByEmail(memberEmail)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "User with this email is not registered"
                                )
                        );

        if (groupMemberRepository
                .existsByGroupAndUser(
                        group,
                        member
                )) {

            throw new BadRequestException(
                    "User is already a member of this group"
            );
        }

        GroupMember membership =
                GroupMember.builder()
                        .group(group)
                        .user(member)
                        .build();

        groupMemberRepository.save(
                membership
        );

        return buildGroupResponse(group);
    }

    @Transactional
    public GroupResponse removeMember(
            String email,
            Long groupId,
            Long userId) {

        User currentUser =
                getCurrentUser(email);

        ExpenseGroup group =
                getGroup(groupId);

        verifyCreator(
                group,
                currentUser
        );

        if (group.getCreatedBy()
                .getId()
                .equals(userId)) {

            throw new BadRequestException(
                    "Group creator cannot be removed"
            );
        }

        User member =
                userRepository
                        .findById(userId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "User not found"
                                )
                        );

        GroupMember membership =
                groupMemberRepository
                        .findByGroupAndUser(
                                group,
                                member
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "User is not a member of this group"
                                )
                        );

        groupMemberRepository.delete(
                membership
        );

        return buildGroupResponse(group);
    }

    @Transactional
    public void deleteGroup(
            String email,
            Long groupId) {

        User currentUser =
                getCurrentUser(email);

        ExpenseGroup group =
                getGroup(groupId);

        verifyCreator(
                group,
                currentUser
        );

        expenseGroupRepository.delete(group);
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

    private void verifyCreator(
            ExpenseGroup group,
            User user) {

        if (!group.getCreatedBy()
                .getId()
                .equals(user.getId())) {

            throw new BadRequestException(
                    "Only the group creator can perform this action"
            );
        }
    }

    private GroupResponse buildGroupResponse(
            ExpenseGroup group) {

        List<GroupMemberResponse> members =
                groupMemberRepository
                        .findByGroupOrderByJoinedAtAsc(
                                group
                        )
                        .stream()
                        .map(member ->
                                new GroupMemberResponse(
                                        member.getId(),
                                        member.getUser().getId(),
                                        member.getUser().getName(),
                                        member.getUser().getEmail(),
                                        member.getJoinedAt()
                                )
                        )
                        .toList();

        return new GroupResponse(
                group.getId(),
                group.getName(),
                group.getDescription(),
                group.getCreatedBy().getId(),
                group.getCreatedBy().getName(),
                group.getCreatedAt(),
                members.size(),
                members
        );
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
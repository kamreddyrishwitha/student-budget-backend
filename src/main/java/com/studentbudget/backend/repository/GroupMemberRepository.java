package com.studentbudget.backend.repository;

import com.studentbudget.backend.entity.ExpenseGroup;
import com.studentbudget.backend.entity.GroupMember;
import com.studentbudget.backend.entity.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupMemberRepository
        extends JpaRepository<GroupMember, Long> {

    boolean existsByGroupAndUser(
            ExpenseGroup group,
            User user
    );

    Optional<GroupMember> findByGroupAndUser(
            ExpenseGroup group,
            User user
    );

    List<GroupMember> findByGroupOrderByJoinedAtAsc(
            ExpenseGroup group
    );

    List<GroupMember> findByUserOrderByJoinedAtDesc(
            User user
    );
}
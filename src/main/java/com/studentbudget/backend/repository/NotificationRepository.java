package com.studentbudget.backend.repository;

import com.studentbudget.backend.entity.Notification;
import com.studentbudget.backend.entity.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository
        extends JpaRepository<Notification, Long> {

    List<Notification>
    findByUserOrderByCreatedAtDesc(
            User user
    );

    Optional<Notification>
    findByIdAndUser(
            Long id,
            User user
    );

    long countByUserAndReadFalse(
            User user
    );

    List<Notification>
    findByUserAndReadFalse(
            User user
    );

    boolean existsByUserAndTitleAndReadFalse(
            User user,
            String title
    );
}
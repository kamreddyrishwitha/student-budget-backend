package com.studentbudget.backend.service;

import com.studentbudget.backend.dto.NotificationResponse;
import com.studentbudget.backend.entity.Budget;
import com.studentbudget.backend.entity.Expense;
import com.studentbudget.backend.entity.Notification;
import com.studentbudget.backend.entity.NotificationType;
import com.studentbudget.backend.entity.User;
import com.studentbudget.backend.exception.ResourceNotFoundException;
import com.studentbudget.backend.repository.BudgetRepository;
import com.studentbudget.backend.repository.ExpenseRepository;
import com.studentbudget.backend.repository.NotificationRepository;
import com.studentbudget.backend.repository.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final BudgetRepository budgetRepository;
    private final ExpenseRepository expenseRepository;

    public NotificationService(
            NotificationRepository notificationRepository,
            UserRepository userRepository,
            BudgetRepository budgetRepository,
            ExpenseRepository expenseRepository) {

        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.budgetRepository = budgetRepository;
        this.expenseRepository = expenseRepository;
    }

    @Transactional
    public List<NotificationResponse> getNotifications(
            String email) {

        User user = getCurrentUser(email);

        generateBudgetNotification(user);

        return notificationRepository
                .findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(this::buildResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(
            String email) {

        User user = getCurrentUser(email);

        return notificationRepository
                .countByUserAndReadFalse(user);
    }

    @Transactional
    public NotificationResponse markAsRead(
            String email,
            Long notificationId) {

        User user = getCurrentUser(email);

        Notification notification =
                notificationRepository
                        .findByIdAndUser(
                                notificationId,
                                user
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Notification not found"
                                )
                        );

        notification.setRead(true);

        Notification saved =
                notificationRepository.save(
                        notification
                );

        return buildResponse(saved);
    }

    @Transactional
    public void markAllAsRead(
            String email) {

        User user = getCurrentUser(email);

        List<Notification> notifications =
                notificationRepository
                        .findByUserAndReadFalse(user);

        for (Notification notification : notifications) {
            notification.setRead(true);
        }

        notificationRepository.saveAll(
                notifications
        );
    }

    private void generateBudgetNotification(
            User user) {

        LocalDate today = LocalDate.now();

        generateBudgetNotificationForMonth(
                user,
                today.getMonthValue(),
                today.getYear()
        );
    }

    private void generateBudgetNotificationForMonth(
            User user,
            int month,
            int year) {

        Budget budget =
                budgetRepository
                        .findByUserAndMonthAndYear(
                                user,
                                month,
                                year
                        )
                        .orElse(null);

        if (budget == null) {
            return;
        }

        YearMonth yearMonth =
                YearMonth.of(year, month);

        LocalDate startDate =
                yearMonth.atDay(1);

        LocalDate endDate =
                yearMonth.atEndOfMonth();

        List<Expense> expenses =
                expenseRepository
                        .findByUserAndExpenseDateBetween(
                                user,
                                startDate,
                                endDate
                        );

        BigDecimal totalExpenses =
                expenses.stream()
                        .map(Expense::getAmount)
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        if (budget.getAmount()
                .compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        BigDecimal usage =
                totalExpenses
                        .multiply(
                                new BigDecimal("100")
                        )
                        .divide(
                                budget.getAmount(),
                                2,
                                RoundingMode.HALF_UP
                        );

        NotificationType type;
        String title;
        String message;

        if (usage.compareTo(
                new BigDecimal("100")) >= 0) {

            type = NotificationType.EXCEEDED;
            title = "Monthly budget exceeded";

            message =
                    "You have used "
                            + usage
                            + "% of your monthly budget.";

        } else if (usage.compareTo(
                new BigDecimal("90")) >= 0) {

            type = NotificationType.NEAR_LIMIT;
            title = "Monthly budget almost reached";

            message =
                    "You have used "
                            + usage
                            + "% of your monthly budget.";

        } else if (usage.compareTo(
                new BigDecimal("75")) >= 0) {

            type = NotificationType.WARNING;
            title = "Monthly budget warning";

            message =
                    "You have used "
                            + usage
                            + "% of your monthly budget.";

        } else {
            return;
        }

        if (notificationRepository
                .existsByUserAndTitleAndReadFalse(
                        user,
                        title
                )) {

            return;
        }

        Notification notification =
                Notification.builder()
                        .title(title)
                        .message(message)
                        .type(type)
                        .read(false)
                        .user(user)
                        .build();

        notificationRepository.save(
                notification
        );
    }

    private NotificationResponse buildResponse(
            Notification notification) {

        return new NotificationResponse(
                notification.getId(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getType().name(),
                notification.isRead(),
                notification.getCreatedAt()
        );
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
}
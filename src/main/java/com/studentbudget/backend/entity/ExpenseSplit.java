package com.studentbudget.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(
        name = "expense_splits",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_expense_split_user",
                        columnNames = {
                                "shared_expense_id",
                                "user_id"
                        }
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpenseSplit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "shared_expense_id",
            nullable = false
    )
    private SharedExpense sharedExpense;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(
            name = "share_amount",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal shareAmount;

    @Column(
            name = "settled_amount",
            nullable = false,
            precision = 12,
            scale = 2
    )
    @Builder.Default
    private BigDecimal settledAmount = BigDecimal.ZERO;
}
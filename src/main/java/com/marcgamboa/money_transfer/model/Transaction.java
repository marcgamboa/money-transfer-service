package com.marcgamboa.money_transfer.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Data
@NoArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "from_account_id")
    @NotNull
    private Account fromAccount;

    @ManyToOne
    @JoinColumn(name = "to_account_id")
    @NotNull
    private Account toAccount;

    @NotNull
    @Column(precision = 20, scale = 2)
    private BigDecimal amount;

    @NotNull
    @Enumerated(EnumType.STRING)
    private Currency sourceCurrency;

    @NotNull
    @Enumerated(EnumType.STRING)
    private Currency targetCurrency;

    @NotNull
    @Column(precision = 20, scale = 2)
    private BigDecimal exchangeRate;

    @NotNull
    @Column(precision = 20, scale = 2)
    private BigDecimal fee;

    @NotNull
    private LocalDateTime timestamp;

    @Enumerated(EnumType.STRING)
    private TransactionStatus status;
} 
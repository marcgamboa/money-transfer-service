package com.marcgamboa.money_transfer.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TransferConstants {
    public static final BigDecimal TRANSACTION_FEE_PERCENTAGE = new BigDecimal("0.01"); // 1%
} 
package com.marcgamboa.money_transfer.service;

import com.marcgamboa.money_transfer.model.Transaction;
import java.math.BigDecimal;

public interface MoneyTransferService {
    Transaction transfer(Long fromAccountId, Long toAccountId, BigDecimal amount, String sourceCurrency);
} 
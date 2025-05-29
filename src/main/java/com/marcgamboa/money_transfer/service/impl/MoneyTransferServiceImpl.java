package com.marcgamboa.money_transfer.service.impl;

import com.marcgamboa.money_transfer.constants.TransferConstants;
import com.marcgamboa.money_transfer.model.*;
import com.marcgamboa.money_transfer.repository.AccountRepository;
import com.marcgamboa.money_transfer.repository.TransactionRepository;
import com.marcgamboa.money_transfer.service.CurrencyConversionService;
import com.marcgamboa.money_transfer.service.MoneyTransferService;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class MoneyTransferServiceImpl implements MoneyTransferService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final CurrencyConversionService currencyConversionService;

    @Override
    @Transactional
    public Transaction transfer(Long fromAccountId, Long toAccountId, BigDecimal amount, String sourceCurrency) {
        // -- to prevent deadlocks
        Long firstLockId = Math.min(fromAccountId, toAccountId);
        Long secondLockId = Math.max(fromAccountId, toAccountId);

        Account firstAccount = accountRepository.findWithLockById(firstLockId)
                .orElseThrow(() -> new RuntimeException("Account not found: " + firstLockId));
        Account secondAccount = accountRepository.findWithLockById(secondLockId)
                .orElseThrow(() -> new RuntimeException("Account not found: " + secondLockId));

        Account fromAccount = (fromAccountId.equals(firstLockId)) ? firstAccount : secondAccount;
        Account toAccount = (toAccountId.equals(firstLockId)) ? firstAccount : secondAccount;
        // --

        Currency sourceAccountCurrency = fromAccount.getCurrency();
        Currency targetAccountCurrency = toAccount.getCurrency();

        // Calculate fee
        BigDecimal fee = amount.multiply(TransferConstants.TRANSACTION_FEE_PERCENTAGE);
        BigDecimal totalDeduction = amount.add(fee);

        // Create transaction record
        Transaction transaction = new Transaction();
        transaction.setFromAccount(fromAccount);
        transaction.setToAccount(toAccount);
        transaction.setAmount(amount);
        transaction.setSourceCurrency(sourceAccountCurrency);
        transaction.setTargetCurrency(targetAccountCurrency);
        transaction.setExchangeRate(
            currencyConversionService.getExchangeRate(sourceAccountCurrency, targetAccountCurrency));
        transaction.setFee(fee);
        transaction.setTimestamp(LocalDateTime.now());
        transaction.setStatus(TransactionStatus.PENDING);

        // Check sufficient funds
        if (fromAccount.getBalance().compareTo(totalDeduction) < 0) {
            transaction.setStatus(TransactionStatus.INSUFFICIENT_FUNDS);
            return transaction;
        }

        // Convert amount if necessary
        BigDecimal convertedAmount = currencyConversionService.convert(
                amount, sourceAccountCurrency, targetAccountCurrency);


        try {
            fromAccount.setBalance(fromAccount.getBalance().subtract(totalDeduction));
            toAccount.setBalance(toAccount.getBalance().add(convertedAmount));

            accountRepository.save(fromAccount);
            accountRepository.save(toAccount);
            
            transaction.setStatus(TransactionStatus.COMPLETED);
        } catch (Exception e) {
            transaction.setStatus(TransactionStatus.FAILED);
            throw new RuntimeException("Failed to process transfer", e);
        }

        return transactionRepository.save(transaction);
    }
} 
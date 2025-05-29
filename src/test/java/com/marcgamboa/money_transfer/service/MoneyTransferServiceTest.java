package com.marcgamboa.money_transfer.service;

import com.marcgamboa.money_transfer.model.Account;
import com.marcgamboa.money_transfer.model.Currency;
import com.marcgamboa.money_transfer.model.Transaction;
import com.marcgamboa.money_transfer.model.TransactionStatus;
import com.marcgamboa.money_transfer.repository.AccountRepository;
import com.marcgamboa.money_transfer.repository.TransactionRepository;
import io.micrometer.common.util.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MoneyTransferServiceTest {

    @Autowired
    private MoneyTransferService moneyTransferService;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    private static final Long ALICE_ID = 1L;
    private static final Long BOB_ID = 2L;

    private static final String INITIAL = "INITIAL";
    private static final String FINAL = "==FINAL";


    @BeforeEach
    void printLine() {
        System.out.println("============================================================================================================================================================================================================================");
    }

    private void setupAccounts(){
            //given

            // Clear existing data in database
            transactionRepository.deleteAll();
            accountRepository.deleteAll();

            Account alice = new Account();
            alice.setId(ALICE_ID);
            alice.setName("Alice");
            alice.setBalance(new BigDecimal("1000.00"));
            alice.setCurrency(Currency.USD);
            accountRepository.save(alice);

            Account bob = new Account();
            bob.setId(BOB_ID);
            bob.setName("Bob");
            bob.setBalance(new BigDecimal("500.00"));
            bob.setCurrency(Currency.JPY);
            accountRepository.save(bob);

            displayBalances(INITIAL, alice, bob);
    }

    @Test
    @Order(1)
    void testTransfer50USDFromAliceToBob() {
        //given
        System.out.println("[Scenario 1] - Transfer 50 USD from Alice to Bob");
        setupAccounts();

        // when
        Transaction transaction = moneyTransferService.transfer(
            ALICE_ID, BOB_ID, new BigDecimal("50.00"), "USD"
        );

        Account updatedAlice = accountRepository.findById(ALICE_ID).orElseThrow();
        Account updatedBob = accountRepository.findById(BOB_ID).orElseThrow();

        BigDecimal expectedAliceBalance = new BigDecimal("1000.00")
                .subtract(new BigDecimal("50.00"))
                .subtract(new BigDecimal("50.00").multiply(new BigDecimal("0.01"))); // 1% fee

        // then
        assertEquals(TransactionStatus.COMPLETED, transaction.getStatus());
        assertEquals(new BigDecimal("50.00"), transaction.getAmount());
        assertEquals(0, expectedAliceBalance.compareTo(updatedAlice.getBalance()));

        // log
        displayTransactions(transaction);
        displayBalances(FINAL, updatedAlice, updatedBob);
    }

    @Test
    @Order(2)
    void testRecurringTransfer50AUDFromBobToAlice() throws Exception {
        //given
        System.out.println("[Scenario 2] - Transfer 50 AUD from Bob to Alice recurring 20 times");
        setupAccounts();

        int numberOfTransfers = 20;
        List<Transaction> transactions = new LinkedList<>();

        for (int i = 0; i < numberOfTransfers; i++) {
            transactions.add(moneyTransferService.transfer(
                    BOB_ID, ALICE_ID, new BigDecimal("50.00"), "AUD"
            ));
        }

        Account updatedBob = accountRepository.findById(BOB_ID).orElseThrow();
        Account updatedAlice = accountRepository.findById(ALICE_ID).orElseThrow();

        // log
        displayTransactions(transactions);
        displayBalances(FINAL, updatedAlice, updatedBob);
    }

    @Test
    @Order(3)
    void testConcurrentTransfers() throws InterruptedException {
        //given
        System.out.println("Scenario 3 - Concurrent Transfers");
        setupAccounts();

        List<Transaction> transactions = new LinkedList<>();

        // Scenario 3: Concurrent transfers
        int numberOfThreads = 3;
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);

        // Transfer 20 AUD from Bob to Alice
        executorService.submit(() -> {
            try {
                transactions.add(moneyTransferService.transfer(BOB_ID, ALICE_ID,
                    new BigDecimal("20.00"), "AUD"));
            } finally {
                latch.countDown();
            }
        });

        // Transfer 40 USD from Alice to Bob
        executorService.submit(() -> {
            try {
                transactions.add(moneyTransferService.transfer(ALICE_ID, BOB_ID,
                    new BigDecimal("40.00"), "USD"));
            } finally {
                latch.countDown();
            }
        });

        // Transfer 40 CNY from Alice to Bob (should fail due to currency mismatch)
        executorService.submit(() -> {
            try {
                transactions.add(moneyTransferService.transfer(ALICE_ID, BOB_ID,
                    new BigDecimal("40.00"), "CNY"));
            } catch (IllegalArgumentException e) {
                // Expected exception
            } finally {
                latch.countDown();
            }
        });

        latch.await();
        executorService.shutdown();

        Account updatedAlice = accountRepository.findById(ALICE_ID).orElseThrow();
        Account updatedBob = accountRepository.findById(BOB_ID).orElseThrow();

        // log
        displayTransactions(transactions);
        displayBalances(FINAL, updatedAlice, updatedBob);
    }

    private void displayBalances(String text, Account... accounts){
        System.out.printf("===============================%s BALANCE================================%n", text.toUpperCase());
        System.out.printf("%-20s %-20s %-20s%n", "Name", "Balance", "Currency");
        System.out.println("------------------------------------------------------------------------------");
        for(Account account: accounts)
            System.out.printf("%-20s %-20s %-20s%n", account.getName(), account.getBalance(), account.getCurrency());
        System.out.println("==============================================================================\n\n");
    }

    private void displayTransactions(Transaction... transactions){
        System.out.println("===========================================================================TRANSACTION=========================================================================");
        System.out.printf("%-20s %-20s %-20s %-20s %-20s %-20s %-20s%n", "From Account", "To Account", "Amount", "Source Currency", "Target Currency", "Fee", "Status");
        System.out.println("---------------------------------------------------------------------------------------------------------------------------------------------------------------");
        for(Transaction transaction : transactions)
            System.out.printf("%-20s %-20s %-20s %-20s %-20s %-20s %-20s%n", transaction.getFromAccount().getName(), transaction.getToAccount().getName(), transaction.getAmount(),
                    transaction.getSourceCurrency(), transaction.getTargetCurrency(), transaction.getFee(), transaction.getStatus());
        System.out.println("===============================================================================================================================================================\n\n");
    }

    private void displayTransactions(List<Transaction> transactions){
        System.out.println("===========================================================================TRANSACTION=========================================================================");
        System.out.printf("%-20s %-20s %-20s %-20s %-20s %-20s %-20s%n", "From Account", "To Account", "Amount", "Source Currency", "Target Currency", "Fee", "Status");
        System.out.println("---------------------------------------------------------------------------------------------------------------------------------------------------------------");
        for(Transaction transaction : transactions)
            System.out.printf("%-20s %-20s %-20s %-20s %-20s %-20s %-20s%n", transaction.getFromAccount().getName(), transaction.getToAccount().getName(), transaction.getAmount(),
                    transaction.getSourceCurrency(), transaction.getTargetCurrency(), transaction.getFee(), transaction.getStatus());
        System.out.println("===============================================================================================================================================================\n\n");
    }
} 
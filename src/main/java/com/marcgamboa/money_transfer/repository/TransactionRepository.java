package com.marcgamboa.money_transfer.repository;

import com.marcgamboa.money_transfer.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
} 
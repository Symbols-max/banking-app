package com.example.banking.service;

import java.sql.Timestamp;
import java.time.Clock;
import java.time.Instant;

import com.example.banking.dto.SingleTransactionResponse;
import com.example.banking.dto.TransactionResponse;
import com.example.banking.mapper.TransactionMapper;
import com.example.banking.model.Account;
import com.example.banking.model.Transaction;
import com.example.banking.model.TransactionType;
import com.example.banking.repository.TransactionRepository;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountService accountService;
    private final TransactionMapper transactionMapper;
    private final Clock clock;

    @Transactional
    public SingleTransactionResponse deposit(String accountNumber, Long amount) {
        validateAmount(amount);
        var updatedAccount = accountService.increaseBalance(accountNumber, amount);

        var transaction = buildTransaction(null, updatedAccount, amount, TransactionType.DEPOSIT);
        transactionRepository.save(transaction);

        return transactionMapper.toSingleDepositTransactionResponse(transaction);
    }

    @Transactional
    public SingleTransactionResponse withdraw(String accountNumber, Long amount) {
        validateAmount(amount);
        var updatedAccount = accountService.decreaseBalance(accountNumber, amount);

        var transaction = buildTransaction(updatedAccount, null, amount, TransactionType.WITHDRAWAL);
        transactionRepository.save(transaction);

        return transactionMapper.toSingleWithdrawTransactionResponse(transaction);
    }

    @Transactional
    public TransactionResponse transfer(String fromAccountNumber, String toAccountNumber, Long amount) {
        validateAmount(amount);
        var transferAccounts = accountService.transferFund(fromAccountNumber, toAccountNumber, amount);

        var transaction =
                buildTransaction(transferAccounts.getFirst(), transferAccounts.getLast(), amount, TransactionType.TRANSFER);
        transactionRepository.save(transaction);

        return transactionMapper.toTransactionResponse(transaction);
    }

    private Transaction buildTransaction(Account fromAccount, Account toAccount, Long amount, TransactionType type) {
        return Transaction.builder()
                          .fromAccount(fromAccount)
                          .toAccount(toAccount)
                          .amount(amount)
                          .type(type)
                          .timestamp(Timestamp.from(Instant.now(clock)))
                          .build();
    }

    private void validateAmount(Long amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Amount cannot be negative");
        }
    }
}

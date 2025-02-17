package com.example.banking.service;

import java.sql.Timestamp;
import java.time.Clock;
import java.time.Instant;
import java.util.List;

import com.example.banking.dto.SingleTransactionResponse;
import com.example.banking.dto.TransactionResponse;
import com.example.banking.mapper.TransactionMapper;
import com.example.banking.model.Account;
import com.example.banking.model.Transaction;
import com.example.banking.model.TransactionType;
import com.example.banking.repository.TransactionRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    private static final String ACCOUNT_NUMBER = "123456";
    private static final String ACCOUNT_NUMBER_2 = "654321";
    private static final Long AMOUNT = 100L;
    private static final Long NEGATIVE_AMOUNT = -100L;
    private static final Account ACCOUNT = Account.builder().accountNumber(ACCOUNT_NUMBER).build();
    private static final Instant INSTANT = Instant.now();
    private static final Timestamp TIMESTAMP = Timestamp.from(INSTANT);

    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private AccountService accountService;
    @Mock
    private TransactionMapper transactionMapper;
    @Mock
    private Clock clock;
    @Captor
    private ArgumentCaptor<Transaction> transactionCaptor;

    @BeforeEach
    void setUp() {
        lenient().when(clock.instant()).thenReturn(INSTANT);
    }

    @InjectMocks
    private TransactionService transactionService;

    @Test
    void depositIncreasesAccountBalance() {
        when(accountService.increaseBalance(ACCOUNT_NUMBER, AMOUNT)).thenReturn(ACCOUNT);
        var singleTransactionResponse = new SingleTransactionResponse(ACCOUNT_NUMBER,
                                                                      AMOUNT,
                                                                      TransactionType.DEPOSIT,
                                                                      TIMESTAMP);
        when(transactionMapper.toSingleDepositTransactionResponse(transactionCaptor.capture()))
                .thenReturn(singleTransactionResponse);

        var response = transactionService.deposit(ACCOUNT_NUMBER, AMOUNT);

        assertEquals(singleTransactionResponse, response);
        var transaction = transactionCaptor.getValue();
        verify(transactionRepository).save(transaction);
        assertNull(transaction.getFromAccount());
        assertEquals(ACCOUNT_NUMBER, transaction.getToAccount().getAccountNumber());
        assertEquals(AMOUNT, transaction.getAmount());
        assertEquals(TransactionType.DEPOSIT, transaction.getType());
        assertEquals(TIMESTAMP, transaction.getTimestamp());
    }

    @Test
    void depositIncreasesAccountBalanceWhenNegativeAmount() {
        var exception = assertThrows(IllegalArgumentException.class,
                                     () -> transactionService.deposit(ACCOUNT_NUMBER, NEGATIVE_AMOUNT));

        assertEquals("Amount cannot be negative", exception.getMessage());
    }

    @Test
    void withdrawDecreasesAccountBalance() {
        when(accountService.decreaseBalance(ACCOUNT_NUMBER, AMOUNT)).thenReturn(ACCOUNT);
        var singleTransactionResponse = new SingleTransactionResponse(ACCOUNT_NUMBER,
                                                                      AMOUNT,
                                                                      TransactionType.WITHDRAWAL,
                                                                      TIMESTAMP);
        when(transactionMapper.toSingleWithdrawTransactionResponse(transactionCaptor.capture()))
                .thenReturn(singleTransactionResponse);

        var response = transactionService.withdraw(ACCOUNT_NUMBER, AMOUNT);

        assertEquals(singleTransactionResponse, response);
        var transaction = transactionCaptor.getValue();
        verify(transactionRepository).save(transaction);
        assertEquals(ACCOUNT_NUMBER, transaction.getFromAccount().getAccountNumber());
        assertNull(transaction.getToAccount());
        assertEquals(AMOUNT, transaction.getAmount());
        assertEquals(TransactionType.WITHDRAWAL, transaction.getType());
        assertEquals(TIMESTAMP, transaction.getTimestamp());
    }

    @Test
    void withdrawDecreasesAccountBalanceWhenNegativeAmount() {
        var exception = assertThrows(IllegalArgumentException.class,
                                     () -> transactionService.withdraw(ACCOUNT_NUMBER, NEGATIVE_AMOUNT));

        assertEquals("Amount cannot be negative", exception.getMessage());
    }

    @Test
    void transferFundsBetweenAccounts() {
        when(accountService.transferFund(ACCOUNT_NUMBER, ACCOUNT_NUMBER_2, AMOUNT))
                .thenReturn(List.of(ACCOUNT,Account.builder().accountNumber(ACCOUNT_NUMBER_2).build()));
        var transactionResponse = new TransactionResponse(ACCOUNT_NUMBER,
                                                          ACCOUNT_NUMBER_2,
                                                          AMOUNT,
                                                          TransactionType.TRANSFER,
                                                          TIMESTAMP);
        when(transactionMapper.toTransactionResponse(transactionCaptor.capture())).thenReturn(transactionResponse);

        var response = transactionService.transfer(ACCOUNT_NUMBER, ACCOUNT_NUMBER_2, AMOUNT);

        assertEquals(transactionResponse, response);
        var transaction = transactionCaptor.getValue();
        verify(transactionRepository).save(transaction);
        assertEquals(ACCOUNT_NUMBER, transaction.getFromAccount().getAccountNumber());
        assertEquals(ACCOUNT_NUMBER_2, transaction.getToAccount().getAccountNumber());
        assertEquals(AMOUNT, transaction.getAmount());
        assertEquals(TransactionType.TRANSFER, transaction.getType());
        assertEquals(TIMESTAMP, transaction.getTimestamp());
    }

    @Test
    void transferFundsBetweenAccountsWhenNegativeAmount() {
        var exception = assertThrows(IllegalArgumentException.class,
                                     () -> transactionService.transfer(ACCOUNT_NUMBER, ACCOUNT_NUMBER_2, NEGATIVE_AMOUNT));

        assertEquals("Amount cannot be negative", exception.getMessage());
    }

}
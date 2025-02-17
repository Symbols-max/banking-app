package com.example.banking.service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.example.banking.dto.AccountDto;
import com.example.banking.dto.TransferAccounts;
import com.example.banking.exception.AccountNotFoundException;
import com.example.banking.exception.InsufficientFundsException;
import com.example.banking.exception.NegativeBalanceException;
import com.example.banking.mapper.AccountMapper;
import com.example.banking.model.Account;
import com.example.banking.repository.AccountRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    private final static Long ACCOUNT_ID = 1L;
    private final static Long BALANCE = 100L;
    private final static String ACCOUNT_NUMBER = UUID.randomUUID().toString();
    private final static String ACCOUNT_NUMBER_2 = UUID.randomUUID().toString();
    private final static AccountDto ACCOUNT_DTO = new AccountDto(ACCOUNT_NUMBER, BALANCE);
    private final static Account ACCOUNT = buildAccount();

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountMapper accountMapper;

    @Captor
    private ArgumentCaptor<Account> accountCaptor;

    @InjectMocks
    private AccountService accountService;

    @Test
    void createAccountWithValidInitialBalanceCreatesAccount() {
        when(accountRepository.save(accountCaptor.capture())).thenReturn(ACCOUNT);
        when(accountMapper.toAccountDto(ACCOUNT)).thenReturn(ACCOUNT_DTO);

        AccountDto result = accountService.createAccount(BALANCE);

        var capturedAccount = accountCaptor.getValue();
        assertEquals(BALANCE, capturedAccount.getBalance());

        assertEquals(ACCOUNT_NUMBER, result.accountNumber());
        assertEquals(BALANCE, result.balance());
    }

    @Test
    void createAccountWithNegativeInitialBalanceThrowsException() {
        var exception = assertThrows(NegativeBalanceException.class, () -> accountService.createAccount(-100L));

        assertEquals("Initial balance cannot be negative", exception.getMessage());
    }

    @Test
    void getAccountWithExistingAccountNumberReturnsAccountByNumber() {
        when(accountRepository.findByAccountNumber(ACCOUNT_NUMBER)).thenReturn(Optional.of(ACCOUNT));
        when(accountMapper.toAccountDto(ACCOUNT)).thenReturn(ACCOUNT_DTO);

        AccountDto result = accountService.getAccount(ACCOUNT_NUMBER);

        assertEquals(ACCOUNT_NUMBER, result.accountNumber());
        assertEquals(BALANCE, result.balance());
    }

    @Test
    void getAccountWithNonExistingAccountByNumberNumberThrowsException() {
        when(accountRepository.findByAccountNumber(ACCOUNT_NUMBER)).thenReturn(Optional.empty());

        var exception = assertThrows(AccountNotFoundException.class, () -> accountService.getAccount(ACCOUNT_NUMBER));

        assertEquals("Account " + ACCOUNT_NUMBER + " not found", exception.getMessage());
    }

    @Test
    void getAllAccountsWithValidPageableReturnsAccountsPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Account> accountPage = new PageImpl<>(Collections.singletonList(ACCOUNT));
        when(accountRepository.findAll(pageable)).thenReturn(accountPage);
        when(accountMapper.toAccountDto(ACCOUNT)).thenReturn(ACCOUNT_DTO);

        Page<AccountDto> result = accountService.getAllAccounts(pageable);

        assertEquals(1, result.getTotalElements());
        var accountDto = result.getContent().getFirst();
        assertEquals(ACCOUNT_NUMBER, accountDto.accountNumber());
        assertEquals(BALANCE, accountDto.balance());
    }

    @Test
    void increaseBalanceWithValidAccountNumberIncreasesBalance() {
        var account = buildAccount();
        when(accountRepository.findByAccountNumber(ACCOUNT_NUMBER)).thenReturn(Optional.of(account));
        when(accountRepository.save(account)).thenReturn(account);

        Account result = accountService.increaseBalance(ACCOUNT_NUMBER, 50L);

        assertEquals(150L, result.getBalance());
    }

    @Test
    void decreaseBalanceWithValidAccountNumberDecreasesBalance() {
        var account = buildAccount();
        when(accountRepository.findByAccountNumber(ACCOUNT_NUMBER)).thenReturn(Optional.of(account));
        when(accountRepository.save(account)).thenReturn(account);

        Account result = accountService.decreaseBalance(ACCOUNT_NUMBER, 50L);

        assertEquals(50L, result.getBalance());
    }

    @Test
    void decreaseBalanceWhenFundAmountIsNotEnough() {
        var account = buildAccount();
        when(accountRepository.findByAccountNumber(ACCOUNT_NUMBER)).thenReturn(Optional.of(account));
        var exception = assertThrows(InsufficientFundsException.class,
                                     () -> accountService.decreaseBalance(ACCOUNT_NUMBER, 150L));

        assertEquals("Insufficient funds for withdrawal", exception.getMessage());
    }

    @Test
    void transferFundWithoutExistingAccounts() {
        when(accountRepository.findAccountsForTransfer(ACCOUNT_NUMBER, ACCOUNT_NUMBER_2)).thenReturn(Optional.empty());

        var exception = assertThrows(AccountNotFoundException.class,
                                     () -> accountService.transferFund(ACCOUNT_NUMBER, ACCOUNT_NUMBER_2, 50L));

        assertEquals("Both accounts not found: " + ACCOUNT_NUMBER + " and " + ACCOUNT_NUMBER_2, exception.getMessage());
    }

    @Test
    void transferFundWithNonExistingFromAccountThrowsException() {
        var transferAccounts = new TransferAccounts.Builder()
                .setFromAccountId(null)
                .setFromAccountNumber(null)
                .setFromAccountBalance(null)
                .setToAccountId(2L)
                .setToAccountNumber(ACCOUNT_NUMBER)
                .setToAccountBalance(BALANCE)
                .build();
        when(accountRepository.findAccountsForTransfer(ACCOUNT_NUMBER_2, ACCOUNT_NUMBER)).thenReturn(Optional.of(transferAccounts));

        var exception = assertThrows(AccountNotFoundException.class,
                                     () -> accountService.transferFund(ACCOUNT_NUMBER_2, ACCOUNT_NUMBER, 50L));

        assertEquals("Account not found: " + ACCOUNT_NUMBER_2, exception.getMessage());
    }

    @Test
    void transferFundWithNonExistingToAccountThrowsException() {
        TransferAccounts transferAccounts = new TransferAccounts.Builder()
                .setFromAccountId(1L)
                .setFromAccountNumber(ACCOUNT_NUMBER)
                .setFromAccountBalance(BALANCE)
                .setToAccountId(null)
                .setToAccountNumber(null)
                .setToAccountBalance(null)
                .build();
        when(accountRepository.findAccountsForTransfer(ACCOUNT_NUMBER, ACCOUNT_NUMBER_2)).thenReturn(Optional.of(transferAccounts));

        var exception = assertThrows(AccountNotFoundException.class,
                                     () -> accountService.transferFund(ACCOUNT_NUMBER, ACCOUNT_NUMBER_2, 50L));

        assertEquals("Account not found: " + ACCOUNT_NUMBER_2, exception.getMessage());
    }

    @Test
    void transferFundWithValidAccountNumbersTransfersFunds() {
        TransferAccounts transferAccounts = new TransferAccounts.Builder()
                .setFromAccountId(1L)
                .setFromAccountNumber(ACCOUNT_NUMBER)
                .setFromAccountBalance(BALANCE)
                .setToAccountId(2L)
                .setToAccountNumber(ACCOUNT_NUMBER_2)
                .setToAccountBalance(BALANCE)
                .build();
        var fromAccount = new Account(1L, ACCOUNT_NUMBER, 50L);
        var toAccount = new Account(1L, ACCOUNT_NUMBER_2, 150L);
        when(accountRepository.findAccountsForTransfer(ACCOUNT_NUMBER, ACCOUNT_NUMBER_2)).thenReturn(Optional.of(transferAccounts));
        when(accountRepository.saveAll(any(List.class))).thenReturn(List.of(fromAccount, toAccount));

        List<Account> result = accountService.transferFund(ACCOUNT_NUMBER, ACCOUNT_NUMBER_2, 50L);

        assertEquals(2, result.size());
        assertEquals(50L, result.get(0).getBalance());
        assertEquals(150L, result.get(1).getBalance());
    }

    private static Account buildAccount() {
        return Account.builder()
                      .id(ACCOUNT_ID)
                      .accountNumber(ACCOUNT_NUMBER)
                      .balance(BALANCE)
                      .build();
    }
}
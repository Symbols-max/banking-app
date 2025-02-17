package com.example.banking.service;

import java.util.List;
import java.util.UUID;
import java.util.function.BiFunction;

import com.example.banking.dto.AccountDto;
import com.example.banking.dto.TransferAccounts;
import com.example.banking.exception.AccountNotFoundException;
import com.example.banking.exception.NegativeBalanceException;
import com.example.banking.mapper.AccountMapper;
import com.example.banking.model.Account;
import com.example.banking.repository.AccountRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;

    @Transactional
    public AccountDto createAccount(Long initialBalance) {
        if (initialBalance < 0) {
            throw new NegativeBalanceException("Initial balance cannot be negative");
        }

        var newAccount = Account.builder()
                                .balance(initialBalance)
                                .accountNumber(UUID.randomUUID().toString())
                                .build();
        var savedAccount = accountRepository.save(newAccount);

        return accountMapper.toAccountDto(savedAccount);
    }

    @Transactional(readOnly = true)
    public Page<AccountDto> getAllAccounts(Pageable pageable) {
        return accountRepository.findAll(pageable)
                                .map(accountMapper::toAccountDto);
    }

    @Transactional(readOnly = true)
    public AccountDto getAccount(String accountNumber) {
        return accountMapper.toAccountDto(getAccountByNumber(accountNumber));
    }

    @Transactional
    public Account increaseBalance(String accountNumber, Long amount) {
        return adjustBalance(accountNumber, amount, Account::increaseBalance);
    }

    @Transactional
    public Account decreaseBalance(String accountNumber, Long amount) {
        return adjustBalance(accountNumber, amount, Account::decreaseBalance);
    }

    private Account adjustBalance(String accountNumber, Long amount, BiFunction<Account, Long, Account> balanceAdjuster) {
        var account = getAccountByNumber(accountNumber);
        return accountRepository.save(balanceAdjuster.apply(account, amount));
    }

    @Transactional
    public List<Account> transferFund(String fromAccountNumber, String toAccountNumber, Long amount) {
        var transferAccounts = getTransferAccountsByNumbers(fromAccountNumber, toAccountNumber);

        var fromAccount = transferAccounts.getFromAccount()
                                          .map(account -> account.decreaseBalance(amount))
                                          .orElseThrow(() -> new AccountNotFoundException(String.format("Account not found: %s",
                                                                                                        fromAccountNumber)));
        var toAccount = transferAccounts.getToAccount()
                                        .map(account -> account.increaseBalance(amount))
                                        .orElseThrow(() -> new AccountNotFoundException(String.format("Account not found: %s",
                                                                                                      toAccountNumber)));

        return accountRepository.saveAll(List.of(fromAccount, toAccount));
    }

    private Account getAccountByNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                                .orElseThrow(
                                        () -> new AccountNotFoundException(String.format("Account %s not found", accountNumber)));
    }

    private TransferAccounts getTransferAccountsByNumbers(String fromAccountNumber, String toAccountNumber) {
        return accountRepository.findAccountsForTransfer(fromAccountNumber, toAccountNumber)
                                .orElseThrow(() -> new AccountNotFoundException(String.format("Both accounts not found: %s and %s",
                                                                                              fromAccountNumber,
                                                                                              toAccountNumber)));

    }
}

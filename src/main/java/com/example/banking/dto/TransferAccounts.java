package com.example.banking.dto;

import java.util.Optional;

import com.example.banking.model.Account;

import org.inferred.freebuilder.FreeBuilder;
import org.springframework.lang.Nullable;

@FreeBuilder
public interface TransferAccounts {

    @Nullable
    Long getFromAccountId();

    @Nullable
    String getFromAccountNumber();

    @Nullable
    Long getFromAccountBalance();

    @Nullable
    Long getToAccountId();

    @Nullable
    String getToAccountNumber();

    @Nullable
    Long getToAccountBalance();

    default Optional<Account> getFromAccount() {
        return createAccount(getFromAccountId(), getFromAccountNumber(), getFromAccountBalance());
    }

    default Optional<Account> getToAccount() {
        return createAccount(getToAccountId(), getToAccountNumber(), getToAccountBalance());
    }

    private Optional<Account> createAccount(Long accountId, String accountNumber, Long balance) {
        if (accountId != null && accountNumber != null && balance != null) {
            return Optional.of(Account.builder()
                                      .id(accountId)
                                      .accountNumber(accountNumber)
                                      .balance(balance)
                                      .build());
        }

        return Optional.empty();
    }

    class Builder extends TransferAccounts_Builder { }
}

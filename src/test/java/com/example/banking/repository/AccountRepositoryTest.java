package com.example.banking.repository;

import java.util.Optional;

import com.example.banking.dto.TransferAccounts;
import com.example.banking.util.TestContainerConfig;
import com.github.database.rider.core.api.configuration.DBUnit;
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.junit5.api.DBRider;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@DBRider
@DBUnit(caseSensitiveTableNames = true, schema = "bank")
@DataSet(value = "datasets/accounts.yml")
class AccountRepositoryTest extends TestContainerConfig {

    private final static String ACCOUNT_NUMBER = "123456789";
    private final static String ACCOUNT_NUMBER_2 = "987654321";
    private final static String MISSED_ACCOUNT_NUMBER = "missed";

    @Autowired
    private AccountRepository accountRepository;

    @Test
    void shouldFindBothAccounts() {
        Optional<TransferAccounts> result = accountRepository.findAccountsForTransfer(ACCOUNT_NUMBER, ACCOUNT_NUMBER_2);

        assertTrue(result.isPresent());
        TransferAccounts transferAccounts = result.get();

        assertEquals(100, transferAccounts.getFromAccountId());
        assertEquals(ACCOUNT_NUMBER, transferAccounts.getFromAccountNumber());
        assertEquals(1000, transferAccounts.getFromAccountBalance());
        assertEquals(101, transferAccounts.getToAccountId());
        assertEquals(ACCOUNT_NUMBER_2, transferAccounts.getToAccountNumber());
        assertEquals(2000, transferAccounts.getToAccountBalance());
    }

    @Test
    void shouldNotFindBothAccounts() {
        Optional<TransferAccounts> result = accountRepository.findAccountsForTransfer(MISSED_ACCOUNT_NUMBER, MISSED_ACCOUNT_NUMBER);

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldNotFindFromAccount() {
        Optional<TransferAccounts> result = accountRepository.findAccountsForTransfer(MISSED_ACCOUNT_NUMBER, ACCOUNT_NUMBER);

        assertTrue(result.isPresent());
        TransferAccounts transferAccounts = result.get();

        assertNull(transferAccounts.getFromAccountId());
        assertNull(transferAccounts.getFromAccountNumber());
        assertNull(transferAccounts.getFromAccountBalance());
        assertEquals(100, transferAccounts.getToAccountId());
        assertEquals(ACCOUNT_NUMBER, transferAccounts.getToAccountNumber());
        assertEquals(1000, transferAccounts.getToAccountBalance());
    }

    @Test
    void shouldNotFindToAccount() {
        Optional<TransferAccounts> result = accountRepository.findAccountsForTransfer(ACCOUNT_NUMBER, MISSED_ACCOUNT_NUMBER);

        assertTrue(result.isPresent());
        TransferAccounts transferAccounts = result.get();

        assertEquals(100, transferAccounts.getFromAccountId());
        assertEquals(ACCOUNT_NUMBER, transferAccounts.getFromAccountNumber());
        assertEquals(1000, transferAccounts.getFromAccountBalance());
        assertNull(transferAccounts.getToAccountId());
        assertNull(transferAccounts.getToAccountNumber());
        assertNull(transferAccounts.getToAccountBalance());
    }
}

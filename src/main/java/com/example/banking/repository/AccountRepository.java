package com.example.banking.repository;

import java.util.Optional;

import com.example.banking.dto.TransferAccounts;
import com.example.banking.model.Account;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import lombok.NonNull;

public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findByAccountNumber(String accountNumber);

    @Query(value = """
            (SELECT a1.id AS fromAccountId,
                    a1.account_number AS fromAccountNumber,
                    a1.balance AS fromAccountBalance,
                    a2.id AS toAccountId,
                    a2.account_number AS toAccountNumber,
                    a2.balance AS toAccountBalance
             FROM accounts a1
             LEFT JOIN accounts a2 ON a2.account_number = :toAccountNumber
             WHERE a1.account_number = :fromAccountNumber)
            
            UNION ALL
            
            (SELECT NULL AS fromAccountId,
                    NULL AS fromAccountNumber,
                    NULL AS fromAccountBalance,
                    a2.id AS toAccountId,
                    a2.account_number AS toAccountNumber,
                    a2.balance AS toAccountBalance
             FROM accounts a2
             WHERE a2.account_number = :toAccountNumber
             AND NOT EXISTS (SELECT 1 FROM accounts a1 WHERE a1.account_number = :fromAccountNumber))
            """, nativeQuery = true)
    Optional<TransferAccounts> findAccountsForTransfer(@Param("fromAccountNumber") String fromAccountNumber,
                                                       @Param("toAccountNumber") String toAccountNumber);


    @NonNull
    Page<Account> findAll(@NonNull Pageable pageable);
}

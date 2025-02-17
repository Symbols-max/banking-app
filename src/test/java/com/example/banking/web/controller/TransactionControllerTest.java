package com.example.banking.web.controller;

import com.example.banking.util.TestContainerConfig;
import com.github.database.rider.core.api.configuration.DBUnit;
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.api.dataset.ExpectedDataSet;
import com.github.database.rider.junit5.api.DBRider;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
@DBRider
@DBUnit(caseSensitiveTableNames = true, schema = "bank")
@DataSet(value = { "datasets/accounts.yml", "datasets/empty_transactions.yml" })
class TransactionControllerTest extends TestContainerConfig {

    @Autowired
    private MockMvc mockMvc;

    private static final String ACCOUNT_NUMBER = "123456789";
    private static final String TO_ACCOUNT_NUMBER = "987654321";
    private static final Long AMOUNT = 100L;

    @Test
    @ExpectedDataSet("datasets/deposit.yml")
    void shouldDepositSuccessfully() throws Exception {
        mockMvc.perform(post("/transactions/deposit")
                                .param("accountNumber", ACCOUNT_NUMBER)
                                .param("amount", String.valueOf(AMOUNT)))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.accountNumber").value(ACCOUNT_NUMBER))
               .andExpect(jsonPath("$.amount").value(AMOUNT))
               .andExpect(jsonPath("$.type").value("DEPOSIT"));
    }

    @Test
    void shouldDepositReturnBadRequestForNegativeAmount() throws Exception {
        mockMvc.perform(post("/transactions/deposit")
                                .param("accountNumber", ACCOUNT_NUMBER)
                                .param("amount", String.valueOf(-100)))
               .andExpect(status().isBadRequest())
               .andExpect(content().string("Amount cannot be negative"));
    }

    @Test
    void shouldDepositReturnNotFoundForMissedAccount() throws Exception {
        mockMvc.perform(post("/transactions/deposit")
                                .param("accountNumber", "missed_account")
                                .param("amount", String.valueOf(AMOUNT)))
               .andExpect(status().isNotFound())
               .andExpect(content().string("Account missed_account not found"));
    }

    @Test
    @ExpectedDataSet("datasets/withdraw.yml")
    void shouldWithdrawSuccessfully() throws Exception {
        mockMvc.perform(post("/transactions/withdraw")
                                .param("accountNumber", ACCOUNT_NUMBER)
                                .param("amount", String.valueOf(AMOUNT)))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.accountNumber").value(ACCOUNT_NUMBER))
               .andExpect(jsonPath("$.amount").value(AMOUNT))
               .andExpect(jsonPath("$.type").value("WITHDRAWAL"));
    }

    @Test
    void shouldWithdrawReturnBadRequestForNegativeAmount() throws Exception {
        mockMvc.perform(post("/transactions/withdraw")
                                .param("accountNumber", ACCOUNT_NUMBER)
                                .param("amount", String.valueOf(-100)))
               .andExpect(status().isBadRequest())
               .andExpect(content().string("Amount cannot be negative"));
    }

    @Test
    void shouldWithdrawReturnBadRequestForNegativeBalance() throws Exception {
        mockMvc.perform(post("/transactions/withdraw")
                                .param("accountNumber", ACCOUNT_NUMBER)
                                .param("amount", String.valueOf(10000)))
               .andExpect(status().isBadRequest())
               .andExpect(content().string("Insufficient funds for withdrawal"));
    }

    @Test
    void shouldWithdrawReturnNotFoundForMissedAccount() throws Exception {
        mockMvc.perform(post("/transactions/withdraw")
                                .param("accountNumber", "missed_account")
                                .param("amount", String.valueOf(AMOUNT)))
               .andExpect(status().isNotFound())
               .andExpect(content().string("Account missed_account not found"));
    }

    @Test
    @ExpectedDataSet("datasets/transfer.yml")
    void shouldTransferSuccessfully() throws Exception {
        mockMvc.perform(post("/transactions/transfer")
                                .param("fromAccountNumber", ACCOUNT_NUMBER)
                                .param("toAccountNumber", TO_ACCOUNT_NUMBER)
                                .param("amount", String.valueOf(AMOUNT)))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.fromAccountNumber").value(ACCOUNT_NUMBER))
               .andExpect(jsonPath("$.toAccountNumber").value(TO_ACCOUNT_NUMBER))
               .andExpect(jsonPath("$.amount").value(AMOUNT))
               .andExpect(jsonPath("$.type").value("TRANSFER"));
    }

    @Test
    void shouldTransferReturnBadRequestForNegativeAmount() throws Exception {
        mockMvc.perform(post("/transactions/transfer")
                                .param("fromAccountNumber", ACCOUNT_NUMBER)
                                .param("toAccountNumber", TO_ACCOUNT_NUMBER)
                                .param("amount", String.valueOf(-100)))
               .andExpect(status().isBadRequest())
               .andExpect(content().string("Amount cannot be negative"));
    }

    @Test
    void shouldTransferReturnBadRequestForNegativeBalance() throws Exception {
        mockMvc.perform(post("/transactions/transfer")
                                .param("fromAccountNumber", ACCOUNT_NUMBER)
                                .param("toAccountNumber", TO_ACCOUNT_NUMBER)
                                .param("amount", String.valueOf(10000)))
               .andExpect(status().isBadRequest())
               .andExpect(content().string("Insufficient funds for withdrawal"));
    }

    @Test
    void shouldTransferReturnNotFoundForMissedBothAccounts() throws Exception {
        mockMvc.perform(post("/transactions/transfer")
                                .param("fromAccountNumber", "missed_account")
                                .param("toAccountNumber", "missed_account_2")
                                .param("amount", String.valueOf(AMOUNT)))
               .andExpect(status().isNotFound())
               .andExpect(content().string("Both accounts not found: missed_account and missed_account_2"));
    }

    @Test
    void shouldTransferReturnNotFoundForMissedFromAccount() throws Exception {
        mockMvc.perform(post("/transactions/transfer")
                                .param("fromAccountNumber", "missed_account")
                                .param("toAccountNumber", TO_ACCOUNT_NUMBER)
                                .param("amount", String.valueOf(AMOUNT)))
               .andExpect(status().isNotFound())
               .andExpect(content().string("Account not found: missed_account"));
    }

    @Test
    void shouldTransferReturnNotFoundForMissedToAccount() throws Exception {
        mockMvc.perform(post("/transactions/transfer")
                                .param("fromAccountNumber", ACCOUNT_NUMBER)
                                .param("toAccountNumber", "missed_account")
                                .param("amount", String.valueOf(AMOUNT)))
               .andExpect(status().isNotFound())
               .andExpect(content().string("Account not found: missed_account"));
    }
}

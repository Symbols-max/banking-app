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

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
@DBRider
@DBUnit(caseSensitiveTableNames = true, schema = "bank")
@DataSet(value = "datasets/accounts.yml")
class AccountControllerTest extends TestContainerConfig {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @ExpectedDataSet(value = "datasets/accounts_created.yml")
    void createAccountWithValidInitialBalanceReturnsCreatedStatus() throws Exception {
        mockMvc.perform(post("/accounts")
                                .param("initialBalance", "100"))
               .andExpect(status().isCreated())
               .andExpect(content().contentType(APPLICATION_JSON))
               .andExpect(jsonPath("$.balance").value(100))
               .andExpect(jsonPath("$.accountNumber").exists());
    }

    @Test
    @ExpectedDataSet(value = "datasets/accounts.yml")
    void createAccountWithNegativeInitialBalanceReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/accounts")
                                .param("initialBalance", "-100"))
               .andExpect(status().isBadRequest())
               .andExpect(content().string("Initial balance cannot be negative"));
    }

    @Test
    void getAccountWithExistingAccountNumberReturnsOkStatus() throws Exception {
        mockMvc.perform(get("/accounts/123456789"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.balance").value(1000))
               .andExpect(jsonPath("$.accountNumber").value("123456789"));
    }

    @Test
    void getAccountWithNonExistingAccountNumberReturnsNotFoundStatus() throws Exception {
        mockMvc.perform(get("/accounts/nonExisting"))
               .andExpect(status().isNotFound())
               .andExpect(content().string("Account nonExisting not found"));
    }

    @Test
    void getAllAccountsWithValidPageableReturnsOkStatus() throws Exception {
        mockMvc.perform(get("/accounts")
                                .param("page", "0")
                                .param("size", "1"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.content.length()").value(1))
               .andExpect(jsonPath("$.content[0].accountNumber").value("123456789"))
               .andExpect(jsonPath("$.content[0].balance").value(1000))
               .andExpect(jsonPath("$.totalElements").value(2))
               .andExpect(jsonPath("$.totalPages").value(2))
               .andExpect(jsonPath("$.pageable.pageSize").value(1))
               .andExpect(jsonPath("$.pageable.pageNumber").value(0))
               .andExpect(jsonPath("$.first").value(true))
               .andExpect(jsonPath("$.last").value(false));
    }


    @Test
    void getAllAccountsWithDefaultPageableReturnsOkStatus() throws Exception {
        mockMvc.perform(get("/accounts"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.content.length()").value(2)) // Перевіряємо кількість елементів у списку
               .andExpect(jsonPath("$.content[0].accountNumber").value("123456789"))
               .andExpect(jsonPath("$.content[0].balance").value(1000))
               .andExpect(jsonPath("$.content[1].accountNumber").value("987654321"))
               .andExpect(jsonPath("$.content[1].balance").value(2000))
               .andExpect(jsonPath("$.totalElements").value(2))
               .andExpect(jsonPath("$.totalPages").value(1))
               .andExpect(jsonPath("$.pageable.pageSize").value(20))
               .andExpect(jsonPath("$.pageable.pageNumber").value(0))
               .andExpect(jsonPath("$.first").value(true))
               .andExpect(jsonPath("$.last").value(true));
    }
}
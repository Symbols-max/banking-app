package com.example.banking.web.controller;

import com.example.banking.dto.AccountDto;
import com.example.banking.service.AccountService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    public ResponseEntity<AccountDto> createAccount(@RequestParam Long initialBalance) {
        return ResponseEntity.status(HttpStatus.CREATED).body(accountService.createAccount(initialBalance));
    }

    @GetMapping("/{accountNumber}")
    public ResponseEntity<AccountDto> getAccount(@PathVariable String accountNumber) {
        return ResponseEntity.ok(accountService.getAccount(accountNumber));
    }

    @GetMapping
    public Page<AccountDto> getAllAccounts(Pageable pageable) {
        return accountService.getAllAccounts(pageable);
    }
}

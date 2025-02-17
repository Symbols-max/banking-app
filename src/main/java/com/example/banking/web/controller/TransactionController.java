package com.example.banking.web.controller;

import com.example.banking.dto.SingleTransactionResponse;
import com.example.banking.dto.TransactionResponse;
import com.example.banking.service.TransactionService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/deposit")
    public ResponseEntity<SingleTransactionResponse> deposit(@RequestParam String accountNumber, @RequestParam Long amount) {
        return ResponseEntity.ok(transactionService.deposit(accountNumber, amount));
    }

    @PostMapping("/withdraw")
    public ResponseEntity<SingleTransactionResponse> withdraw(@RequestParam String accountNumber, @RequestParam Long amount) {
        return ResponseEntity.ok(transactionService.withdraw(accountNumber, amount));
    }

    @PostMapping("/transfer")
    public ResponseEntity<TransactionResponse> transfer(@RequestParam String fromAccountNumber,
                                                        @RequestParam String toAccountNumber,
                                                        @RequestParam Long amount) {
        return ResponseEntity.ok(transactionService.transfer(fromAccountNumber, toAccountNumber, amount));
    }
}

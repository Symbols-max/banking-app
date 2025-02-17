package com.example.banking.dto;

import java.sql.Timestamp;

import com.example.banking.model.TransactionType;

public record TransactionResponse(String fromAccountNumber,
        String toAccountNumber,
        Long amount,
        TransactionType type,
        Timestamp timestamp) {
}

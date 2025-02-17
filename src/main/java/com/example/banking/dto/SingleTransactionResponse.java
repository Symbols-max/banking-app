package com.example.banking.dto;

import java.sql.Timestamp;

import com.example.banking.model.TransactionType;

public record SingleTransactionResponse(String accountNumber,
        Long amount,
        TransactionType type,
        Timestamp timestamp) { }


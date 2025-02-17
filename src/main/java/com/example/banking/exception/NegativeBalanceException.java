package com.example.banking.exception;

public class NegativeBalanceException extends RuntimeException {

    public NegativeBalanceException(String message) {
        super(message);
    }
}

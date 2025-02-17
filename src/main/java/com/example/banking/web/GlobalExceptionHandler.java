package com.example.banking.web;

import com.example.banking.exception.AccountNotFoundException;
import com.example.banking.exception.InsufficientFundsException;
import com.example.banking.exception.NegativeBalanceException;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AccountNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleAccountNotFoundException(AccountNotFoundException ex) {
        return ex.getMessage();
    }

    @ExceptionHandler(value = { InsufficientFundsException.class, NegativeBalanceException.class, IllegalArgumentException.class })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleInsufficientFundsException(RuntimeException ex) {
        return ex.getMessage();
    }
}


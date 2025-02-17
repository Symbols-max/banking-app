package com.example.banking.mapper;

import com.example.banking.dto.SingleTransactionResponse;
import com.example.banking.dto.TransactionResponse;
import com.example.banking.model.Transaction;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TransactionMapper {

    @Mapping(source = "toAccount.accountNumber", target = "accountNumber")
    SingleTransactionResponse toSingleDepositTransactionResponse(Transaction transaction);

    @Mapping(source = "fromAccount.accountNumber", target = "accountNumber")
    SingleTransactionResponse toSingleWithdrawTransactionResponse(Transaction transaction);

    @Mapping(source = "fromAccount.accountNumber", target = "fromAccountNumber")
    @Mapping(source = "toAccount.accountNumber", target = "toAccountNumber")
    TransactionResponse toTransactionResponse(Transaction transaction);
}

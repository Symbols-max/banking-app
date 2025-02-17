package com.example.banking.mapper;

import com.example.banking.dto.AccountDto;
import com.example.banking.model.Account;

import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AccountMapper {

    AccountDto toAccountDto(Account account);
}

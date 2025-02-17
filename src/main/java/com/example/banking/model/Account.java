package com.example.banking.model;

import com.example.banking.exception.InsufficientFundsException;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "accounts")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String accountNumber;

    @Column(nullable = false)
    private Long balance;

    public Account increaseBalance(Long amount) {
        this.balance += amount;
        return this;
    }

    public Account decreaseBalance(Long amount) {
        if (this.balance < amount) {
            throw new InsufficientFundsException("Insufficient funds for withdrawal");
        }
        this.balance -= amount;
        return this;
    }
}

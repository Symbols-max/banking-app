DROP SCHEMA IF EXISTS bank;
CREATE SCHEMA IF NOT EXISTS bank;
USE bank;

DROP TABLE IF EXISTS transactions;
DROP TABLE IF EXISTS accounts;

CREATE TABLE accounts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    account_number VARCHAR(50) UNIQUE NOT NULL,
    balance BIGINT NOT NULL
);

INSERT INTO accounts (account_number, balance) VALUES ('123456', 5000);
INSERT INTO accounts (account_number, balance) VALUES ('654321', 3000);
INSERT INTO accounts (account_number, balance) VALUES ('111111', 10000);

CREATE TABLE transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    from_account_id BIGINT NULL,
    to_account_id BIGINT NULL,
    amount BIGINT NOT NULL,
    type ENUM('DEPOSIT', 'WITHDRAWAL', 'TRANSFER') NOT NULL,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT fk_transactions_from FOREIGN KEY (from_account_id) REFERENCES accounts(id) ON DELETE SET NULL,
    CONSTRAINT fk_transactions_to FOREIGN KEY (to_account_id) REFERENCES accounts(id) ON DELETE SET NULL
);

package com.nicolas.bankingws.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class Transaction {

    public enum Type {
        DEBIT,
        CREDIT
    }

    private final String transactionId;
    private final Type type;
    private final BigDecimal amount;
    private final String counterpartyAccount;
    private final OffsetDateTime timestamp;

    public Transaction(String transactionId, Type type, BigDecimal amount, String counterpartyAccount) {
        this.transactionId = transactionId;
        this.type = type;
        this.amount = amount;
        this.counterpartyAccount = counterpartyAccount;
        this.timestamp = OffsetDateTime.now();
    }

    public String getTransactionId() {
        return transactionId;
    }

    public Type getType() {
        return type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCounterpartyAccount() {
        return counterpartyAccount;
    }

    public OffsetDateTime getTimestamp() {
        return timestamp;
    }
}

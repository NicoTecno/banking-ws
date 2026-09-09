package com.nicolas.bankingws.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Modelo de dominio interno. A propósito NO es la misma clase que genera
 * JAXB desde el XSD (BalanceResponse, etc.) — esa es la representación de
 * "cómo se ve por afuera"; esta es "qué es por dentro". Es la misma
 * separación entidad/DTO que ya viste en el proyecto de REST, solo que acá
 * el "DTO" no lo escribís vos, lo genera el contrato.
 */
public class Account {

    private final String accountNumber;
    private final String ownerName;
    private BigDecimal balance;
    private final String currency;

    public Account(String accountNumber, String ownerName, BigDecimal balance, String currency) {
        this.accountNumber = accountNumber;
        this.ownerName = ownerName;
        this.balance = balance.setScale(2, RoundingMode.HALF_UP);
        this.currency = currency;
    }

    public void debit(BigDecimal amount) {
        this.balance = this.balance.subtract(amount).setScale(2, RoundingMode.HALF_UP);
    }

    public void credit(BigDecimal amount) {
        this.balance = this.balance.add(amount).setScale(2, RoundingMode.HALF_UP);
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public String getCurrency() {
        return currency;
    }
}

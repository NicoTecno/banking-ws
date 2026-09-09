package com.nicolas.bankingws.service;

import com.nicolas.bankingws.exception.AccountNotFoundException;
import com.nicolas.bankingws.exception.InsufficientFundsException;
import com.nicolas.bankingws.model.Account;
import com.nicolas.bankingws.model.Transaction;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AccountService {

    private final Map<String, Account> accounts = new ConcurrentHashMap<>();
    private final Map<String, List<Transaction>> history = new ConcurrentHashMap<>();

    public AccountService() {
        seed("ACC-1001", "Nicolás Ferreyra", new BigDecimal("500000.00"), "ARS");
        seed("ACC-1002", "Julián Torres", new BigDecimal("120000.00"), "ARS");
        seed("ACC-1003", "Carla Gimenez", new BigDecimal("75000.00"), "ARS");
    }

    private void seed(String number, String owner, BigDecimal balance, String currency) {
        accounts.put(number, new Account(number, owner, balance, currency));
        history.put(number, new ArrayList<>());
    }

    public Account findAccount(String accountNumber) {
        Account account = accounts.get(accountNumber);
        if (account == null) {
            throw new AccountNotFoundException(accountNumber);
        }
        return account;
    }

    public List<Transaction> getHistory(String accountNumber) {
        findAccount(accountNumber); // valida que exista, tira AccountNotFoundException si no
        return history.get(accountNumber);
    }

    public synchronized Account transfer(String fromAccountNumber, String toAccountNumber, BigDecimal amount) {
        Account from = findAccount(fromAccountNumber);
        Account to = findAccount(toAccountNumber);

        if (from.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException(fromAccountNumber, from.getBalance(), amount);
        }

        from.debit(amount);
        to.credit(amount);

        String transactionId = UUID.randomUUID().toString();
        history.get(fromAccountNumber).add(new Transaction(transactionId, Transaction.Type.DEBIT, amount, toAccountNumber));
        history.get(toAccountNumber).add(new Transaction(transactionId, Transaction.Type.CREDIT, amount, fromAccountNumber));

        return from;
    }
}

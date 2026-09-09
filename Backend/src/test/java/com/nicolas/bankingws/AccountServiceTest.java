package com.nicolas.bankingws;

import com.nicolas.bankingws.exception.AccountNotFoundException;
import com.nicolas.bankingws.exception.InsufficientFundsException;
import com.nicolas.bankingws.model.Account;
import com.nicolas.bankingws.service.AccountService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AccountServiceTest {

    private final AccountService service = new AccountService();

    @Test
    void findsUnaCuentaSembrada() {
        Account account = service.findAccount("ACC-1001");
        assertThat(account.getOwnerName()).isEqualTo("Nicolás Ferreyra");
        assertThat(account.getBalance()).isEqualByComparingTo("500000.00");
    }

    @Test
    void cuentaInexistenteTiraAccountNotFound() {
        assertThatThrownBy(() -> service.findAccount("ACC-9999"))
                .isInstanceOf(AccountNotFoundException.class);
    }

    @Test
    void transferMuevePlataYRegistraHistorial() {
        Account result = service.transfer("ACC-1001", "ACC-1002", new BigDecimal("1000.00"));

        assertThat(result.getBalance()).isEqualByComparingTo("499000.00");
        assertThat(service.findAccount("ACC-1002").getBalance()).isEqualByComparingTo("121000.00");

        assertThat(service.getHistory("ACC-1001")).hasSize(1);
        assertThat(service.getHistory("ACC-1002")).hasSize(1);
    }

    @Test
    void transferSinFondosTiraInsufficientFunds() {
        assertThatThrownBy(() -> service.transfer("ACC-1003", "ACC-1001", new BigDecimal("999999.00")))
                .isInstanceOf(InsufficientFundsException.class);
    }
}

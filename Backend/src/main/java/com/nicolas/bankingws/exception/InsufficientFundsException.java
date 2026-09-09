package com.nicolas.bankingws.exception;

import org.springframework.ws.soap.server.endpoint.annotation.FaultCode;
import org.springframework.ws.soap.server.endpoint.annotation.SoapFault;

import java.math.BigDecimal;

@SoapFault(faultCode = FaultCode.CLIENT)
public class InsufficientFundsException extends RuntimeException {

    public InsufficientFundsException(String accountNumber, BigDecimal balance, BigDecimal requested) {
        super("La cuenta " + accountNumber + " tiene " + balance
                + " y se pidió transferir " + requested + " — fondos insuficientes");
    }
}

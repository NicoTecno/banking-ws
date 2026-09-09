package com.nicolas.bankingws.exception;

import org.springframework.ws.soap.server.endpoint.annotation.FaultCode;
import org.springframework.ws.soap.server.endpoint.annotation.SoapFault;

/**
 * @SoapFault convierte esta excepción en un <soap:Fault> automáticamente —
 * no hay un @ExceptionHandler central como en REST; acá la anotación vive
 * en la excepción misma. FaultCode.CLIENT es el equivalente SOAP a "la
 * culpa es de quien preguntó" (mandó un número de cuenta que no existe),
 * distinto de FaultCode.SERVER ("algo se rompió de este lado").
 */
@SoapFault(faultCode = FaultCode.CLIENT)
public class AccountNotFoundException extends RuntimeException {

    public AccountNotFoundException(String accountNumber) {
        super("No existe la cuenta " + accountNumber);
    }
}

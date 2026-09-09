package com.nicolas.bankingws.endpoint;

import com.nicolas.bankingws.contract.*;
import com.nicolas.bankingws.model.Account;
import com.nicolas.bankingws.service.AccountService;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import java.time.ZoneOffset;
import java.util.GregorianCalendar;

/**
 * Un método por operación del WSDL. @PayloadRoot es el equivalente acá de
 * @GetMapping en REST — pero en vez de matchear un verbo + un path,
 * matchea el namespace + el nombre del elemento raíz del XML que llega.
 * Las clases BalanceRequest, TransferResponse, etc. que se importan de
 * "contract" NO están escritas a mano en este proyecto — las genera JAXB
 * desde banking.xsd en cada build (target/generated-sources/jaxb).
 */
@Endpoint
public class BankingEndpoint {

    private static final String NAMESPACE_URI = "http://bankingws.nicolas.dev/schemas";

    private final AccountService accountService;

    public BankingEndpoint(AccountService accountService) {
        this.accountService = accountService;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "balanceRequest")
    @ResponsePayload
    public BalanceResponse getBalance(@RequestPayload BalanceRequest request) {
        Account account = accountService.findAccount(request.getAccountNumber());

        BalanceResponse response = new BalanceResponse();
        response.setAccountNumber(account.getAccountNumber());
        response.setOwnerName(account.getOwnerName());
        response.setBalance(account.getBalance());
        response.setCurrency(account.getCurrency());
        return response;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "transferRequest")
    @ResponsePayload
    public TransferResponse transfer(@RequestPayload TransferRequest request) {
        Account updated = accountService.transfer(request.getFromAccount(), request.getToAccount(), request.getAmount());

        TransferResponse response = new TransferResponse();
        response.setTransactionId(java.util.UUID.randomUUID().toString());
        response.setStatus("COMPLETED");
        response.setFromAccountNewBalance(updated.getBalance());
        return response;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "transactionHistoryRequest")
    @ResponsePayload
    public TransactionHistoryResponse getHistory(@RequestPayload TransactionHistoryRequest request) {
        TransactionHistoryResponse response = new TransactionHistoryResponse();

        for (com.nicolas.bankingws.model.Transaction tx : accountService.getHistory(request.getAccountNumber())) {
            response.getTransaction().add(toContractTransaction(tx));
        }
        return response;
    }

    /**
     * Convierte el Transaction interno (com.nicolas.bankingws.model) al
     * Transaction generado por JAXB (com.nicolas.bankingws.contract) —
     * mismo nombre de clase, dos paquetes distintos, a propósito: uno es
     * el modelo de dominio, el otro es lo que el XSD define que viaja por
     * la red. No son intercambiables aunque se llamen igual.
     */
    private Transaction toContractTransaction(com.nicolas.bankingws.model.Transaction tx) {
        Transaction contractTx = new Transaction();
        contractTx.setTransactionId(tx.getTransactionId());
        contractTx.setType(tx.getType().name());
        contractTx.setAmount(tx.getAmount());
        contractTx.setCounterpartyAccount(tx.getCounterpartyAccount());
        contractTx.setTimestamp(toXmlCalendar(tx.getTimestamp()));
        return contractTx;
    }

    private javax.xml.datatype.XMLGregorianCalendar toXmlCalendar(java.time.OffsetDateTime dateTime) {
        try {
            GregorianCalendar calendar = GregorianCalendar.from(dateTime.atZoneSameInstant(ZoneOffset.UTC));
            return DatatypeFactory.newInstance().newXMLGregorianCalendar(calendar);
        } catch (DatatypeConfigurationException e) {
            throw new IllegalStateException("No se pudo convertir la fecha a XML", e);
        }
    }
}

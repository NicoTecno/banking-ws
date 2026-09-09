# Banking WS — SOAP contract-first con WS-Security

Proyecto 3 de 8 del roadmap de arquitecturas de API. Servicio bancario
(saldo, transferencias, historial) diseñado **contract-first de verdad**:
el XSD se escribe primero, y de ahí nacen tanto el WSDL como las clases
Java — nunca al revés.

**Stack:** Java 21 · Spring Boot 4.1.1 · Spring-WS · WSS4J (WS-Security) · JAXB

---

## 1. Qué es SOAP (y en qué se aparta de REST)

SOAP no es un estilo como REST — es un **protocolo**, con reglas fijas:
todo mensaje es un XML con forma de sobre (`Envelope` → `Header` +
`Body`), y el contrato completo (qué operaciones existen, qué forma tiene
cada mensaje) se describe en un **WSDL**, un documento que un cliente
puede leer para generar código automáticamente en su propio lenguaje —
por eso SOAP sigue vivo en banca y gobierno: dos sistemas escritos en
lenguajes distintos pueden integrarse sin coordinarse a mano sobre el
formato.

Dos cosas que REST no tiene de fábrica y este proyecto muestra:

- **Contrato formal y verificable** — un WSDL/XSD es un documento que se
  puede validar automáticamente; un `README` describiendo una REST API no.
- **Seguridad a nivel de mensaje** (WS-Security) — la credencial viaja
  dentro del XML, no en un header HTTP. Sobrevive aunque el mensaje pase
  por intermediarios que no mantienen HTTPS de punta a punta.

## 2. Cómo se implementó

### 2.1 El contrato: `src/main/resources/banking.xsd`

Achica todo a 3 operaciones: `balanceRequest`/`Response` (consulta),
`transferRequest`/`Response`, `transactionHistoryRequest`/`Response`. Este
archivo es la única fuente de verdad — nunca se escriben a mano las clases
Java que representan estos mensajes.

### 2.2 De XSD a Java: el plugin JAXB en `pom.xml`

```xml
<plugin>
    <groupId>org.codehaus.mojo</groupId>
    <artifactId>jaxb2-maven-plugin</artifactId>
    <version>4.1.0</version>
    ...
</plugin>
```

En cada `mvn compile`, este plugin lee `banking.xsd` y genera
`BalanceRequest.java`, `TransferResponse.java`, etc. en
`target/generated-sources/jaxb/com/nicolas/bankingws/contract/` — **esos
archivos no existen en el repo**, nacen del XSD en cada build. Si mañana
agregás un campo al XSD, la clase Java lo tiene automáticamente en el
próximo build; nunca hay que sincronizar contrato y código a mano, porque
literalmente son la misma cosa vista en dos formatos.

### 2.3 De XSD a WSDL: `WebServiceConfig`

`DefaultWsdl11Definition` arma el WSDL completo combinando el XSD (los
tipos de datos) con esta configuración (dónde vive el servicio, qué
nombre tiene el puerto). Nunca se escribe el WSDL a mano — se sirve solo
en `/ws/banking.wsdl`.

### 2.4 Los endpoints: `BankingEndpoint`

```java
@PayloadRoot(namespace = NAMESPACE_URI, localPart = "balanceRequest")
@ResponsePayload
public BalanceResponse getBalance(@RequestPayload BalanceRequest request) { ... }
```

`@PayloadRoot` es el equivalente acá de `@GetMapping` en REST — pero en
vez de matchear verbo HTTP + path, matchea namespace + nombre del
elemento raíz del `<Body>` que llega. Un método por operación.

### 2.5 WS-Security: `WsSecurityConfig`

`Wss4jSecurityInterceptor` valida que todo mensaje entrante traiga un
`<wsse:UsernameToken>` válido en el `<Header>` — si falta o la contraseña
no coincide, el mensaje ni siquiera llega al endpoint, vuelve como un
SOAP Fault de autenticación. Usuario de prueba: `nicolas` / `banking123`.

Usa `PasswordText` (la contraseña viaja en texto plano dentro del XML) a
propósito, para poder armar el pedido a mano en Postman sin herramientas
de WS-Security aparte. Un sistema real usaría `PasswordDigest` (un hash
con nonce y timestamp, que ningún cliente HTTP genérico puede armar a
mano) combinado con HTTPS obligatorio — lo dejo anotado como próximo paso.

### 2.6 SOAP Faults: las excepciones

```java
@SoapFault(faultCode = FaultCode.CLIENT)
public class AccountNotFoundException extends RuntimeException { ... }
```

Nada de `@ExceptionHandler` central como en REST — acá la anotación vive
en la excepción misma, y Spring-WS arma el `<soap:Fault>` solo.
`FaultCode.CLIENT` es "la culpa es de quien preguntó" (un número de
cuenta que no existe); existe también `SERVER` para "se rompió algo de
este lado".

## 3. Cómo correrlo

Igual que con REST: este entorno no tiene salida a Maven Central, así que
escribí todo con mucho cuidado (confirmando cada artifact contra la
documentación oficial — de hecho encontré y corregí un cambio de nombre:
`spring-boot-starter-web-services` quedó deprecado en Boot 4 a favor de
`spring-boot-starter-webservices`, sin guión) pero no lo vi compilar de
verdad. En tu máquina:

```bash
cd banking-ws
mvn spring-boot:run
```

Levanta en `http://localhost:8081`. El WSDL queda en:

```
http://localhost:8081/ws/banking.wsdl
```

Abrilo en el navegador — es la forma más rápida de confirmar que todo
arrancó bien, y de ver el contrato completo que Spring generó.

## 4. Probarlo en Postman

Method `POST`, URL `http://localhost:8081/ws`, header `Content-Type: text/xml; charset=UTF-8`, body tipo **raw**. Los tres tienen que incluir el header de seguridad — sin él, `Wss4jSecurityInterceptor` rechaza el mensaje antes de que llegue al endpoint.

**Consultar saldo:**

```xml
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/">
  <soapenv:Header>
    <wsse:Security xmlns:wsse="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd" soapenv:mustUnderstand="1">
      <wsse:UsernameToken>
        <wsse:Username>nicolas</wsse:Username>
        <wsse:Password Type="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordText">banking123</wsse:Password>
      </wsse:UsernameToken>
    </wsse:Security>
  </soapenv:Header>
  <soapenv:Body>
    <balanceRequest xmlns="http://bankingws.nicolas.dev/schemas">
      <accountNumber>ACC-1001</accountNumber>
    </balanceRequest>
  </soapenv:Body>
</soapenv:Envelope>
```

**Transferir** (mismo `Header` de arriba, cambia el `Body`):

```xml
<transferRequest xmlns="http://bankingws.nicolas.dev/schemas">
  <fromAccount>ACC-1001</fromAccount>
  <toAccount>ACC-1002</toAccount>
  <amount>5000.00</amount>
</transferRequest>
```

**Historial** (mismo `Header`):

```xml
<transactionHistoryRequest xmlns="http://bankingws.nicolas.dev/schemas">
  <accountNumber>ACC-1001</accountNumber>
</transactionHistoryRequest>
```

Probá también sacar el `<wsse:Security>` del header, o poner mal la
contraseña — ahí vas a ver el fault de autenticación en acción. Y probá
`accountNumber` con un valor que no exista — ese es el
`AccountNotFoundException` convertido en fault.

Cuentas de prueba ya cargadas: `ACC-1001` (Nicolás, $500.000), `ACC-1002`
(Julián, $120.000), `ACC-1003` (Carla, $75.000).

## 5. Tests

`AccountServiceTest` cubre la lógica de negocio pura (transferencias,
cuenta inexistente, fondos insuficientes) sin meter XML ni WS-Security en
el medio — separar esa complejidad de la lógica de negocio es la misma
idea de capas que ya venís viendo en los otros proyectos.

## 6. Próximos pasos posibles

- `PasswordDigest` en vez de `PasswordText` (necesita un cliente que sepa
  armar el hash con nonce, Postman solo no alcanza).
- Firma XML (`Signature`) además de autenticación — la otra mitad real de
  WS-Security.
- Un cliente Java generado con `wsimport`/JAXB a partir del WSDL, para ver
  el contrato funcionando de punta a punta en el mismo lenguaje.

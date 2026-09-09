# banking-ws 🏦

Proyecto de aprendizaje de **arquitecturas de API** — implementación de un servicio bancario con **SOAP / contract-first / WS-Security**.

Forma parte de una serie de proyectos que comparan distintos estilos de API (REST, SOAP, GraphQL, gRPC) sobre el mismo dominio de negocio bancario.

---

## Stack

| Capa | Tecnología |
|---|---|
| Backend | Java 21 · Spring Boot 4.1.1 · Spring-WS 5 · Wss4j · JAXB |
| Frontend | React 18 · Vite · Vanilla CSS |
| Protocolo | SOAP 1.1 · WS-Security (UsernameToken / PasswordText) |
| Contrato | Contract-first — las clases Java se generan desde `banking.xsd` |

---

## Estructura del proyecto

```
banking-ws/
├── Backend/                        # Spring Boot — servicio SOAP
│   └── src/main/
│       ├── java/com/nicolas/bankingws/
│       │   ├── config/
│       │   │   ├── CorsConfig.java         # CORS a nivel de Filter (no MVC)
│       │   │   ├── WebServiceConfig.java   # MessageDispatcherServlet + WSDL dinámico
│       │   │   └── WsSecurityConfig.java   # Wss4jSecurityInterceptor (UsernameToken)
│       │   ├── endpoint/
│       │   │   └── BankingEndpoint.java    # 3 operaciones: balance, transfer, history
│       │   ├── exception/
│       │   │   ├── AccountNotFoundException.java
│       │   │   └── InsufficientFundsException.java
│       │   ├── model/
│       │   │   ├── Account.java
│       │   │   └── Transaction.java
│       │   └── service/
│       │       └── AccountService.java     # Lógica de negocio + datos en memoria
│       └── resources/
│           ├── application.yml
│           └── banking.xsd                 # Contrato — fuente de verdad del API
└── Frontend/                       # React + Vite — terminal bancaria
    └── src/
        ├── components/
        │   ├── BalanceForm.jsx
        │   ├── CredentialsBar.jsx
        │   ├── HistoryForm.jsx
        │   ├── TransferForm.jsx
        │   └── XmlViewer.jsx       # Muestra el XML crudo de request y response
        ├── soap.js                 # Cliente SOAP manual (fetch + DOMParser)
        └── App.jsx
```

---

## Cómo correr el proyecto

### Backend

**Requisitos:** Java 21+, Maven

```bash
cd Backend
mvn spring-boot:run
```

El servidor arranca en **`http://localhost:8081`**.

- Endpoint SOAP: `http://localhost:8081/ws`
- WSDL generado dinámicamente: `http://localhost:8081/ws/banking.wsdl`

### Frontend

**Requisitos:** Node 18+

```bash
cd Frontend
npm install
npm run dev
```

La app arranca en **`http://localhost:5173`**.

> El frontend espera el backend en `http://localhost:8081/ws`. Se puede sobrescribir con la variable de entorno `VITE_SOAP_URL` en un archivo `.env`.

---

## Cuentas de demo

Los datos son **en memoria** — se resetean al reiniciar el backend.

| N° de Cuenta | Titular | Saldo inicial | Moneda |
|---|---|---|---|
| `ACC-1001` | Nicolás Ferreyra | $500,000.00 | ARS |
| `ACC-1002` | Julián Torres | $120,000.00 | ARS |
| `ACC-1003` | Carla Gimenez | $75,000.00 | ARS |

---

## Credenciales WS-Security

El header `<wsse:UsernameToken>` es obligatorio en cada request. Sin él el servidor devuelve un SOAP Fault.

| Usuario | Contraseña |
|---|---|
| `nicolas` | `banking123` |

> **Nota:** Se usa `PasswordText` (contraseña en texto plano dentro del XML) para poder probarse fácilmente en Postman. Un sistema real usaría `PasswordDigest` + HTTPS obligatorio.

---

## Operaciones disponibles

Todas las operaciones son `POST http://localhost:8081/ws` con `Content-Type: text/xml`.

### 1. Consultar saldo

```xml
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                  xmlns:tns="http://bankingws.nicolas.dev/schemas"
                  xmlns:wsse="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd">
  <soapenv:Header>
    <wsse:Security>
      <wsse:UsernameToken>
        <wsse:Username>nicolas</wsse:Username>
        <wsse:Password Type="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordText">banking123</wsse:Password>
      </wsse:UsernameToken>
    </wsse:Security>
  </soapenv:Header>
  <soapenv:Body>
    <tns:balanceRequest>
      <tns:accountNumber>ACC-1001</tns:accountNumber>
    </tns:balanceRequest>
  </soapenv:Body>
</soapenv:Envelope>
```

### 2. Transferir fondos

```xml
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                  xmlns:tns="http://bankingws.nicolas.dev/schemas"
                  xmlns:wsse="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd">
  <soapenv:Header>
    <wsse:Security>
      <wsse:UsernameToken>
        <wsse:Username>nicolas</wsse:Username>
        <wsse:Password Type="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordText">banking123</wsse:Password>
      </wsse:UsernameToken>
    </wsse:Security>
  </soapenv:Header>
  <soapenv:Body>
    <tns:transferRequest>
      <tns:fromAccount>ACC-1001</tns:fromAccount>
      <tns:toAccount>ACC-1002</tns:toAccount>
      <tns:amount>10000.00</tns:amount>
    </tns:transferRequest>
  </soapenv:Body>
</soapenv:Envelope>
```

### 3. Historial de movimientos

```xml
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                  xmlns:tns="http://bankingws.nicolas.dev/schemas"
                  xmlns:wsse="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd">
  <soapenv:Header>
    <wsse:Security>
      <wsse:UsernameToken>
        <wsse:Username>nicolas</wsse:Username>
        <wsse:Password Type="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordText">banking123</wsse:Password>
      </wsse:UsernameToken>
    </wsse:Security>
  </soapenv:Header>
  <soapenv:Body>
    <tns:transactionHistoryRequest>
      <tns:accountNumber>ACC-1001</tns:accountNumber>
    </tns:transactionHistoryRequest>
  </soapenv:Body>
</soapenv:Envelope>
```

---

## Errores conocidos y cómo resolverlos

### `NoClassDefFoundError: javax/wsdl/extensions/ExtensibilityElement`

`DefaultWsdl11Definition` depende de `wsdl4j` que en Spring Boot 4 + Spring-WS 5 ya no viene como dependencia transitiva. **Solución:** agregar al `pom.xml`:

```xml
<dependency>
    <groupId>wsdl4j</groupId>
    <artifactId>wsdl4j</artifactId>
    <version>1.6.3</version>
</dependency>
```

### `class CorsConfig is public, should be declared in a file named CorsConfig.java`

El archivo `CorsConfig-banking-ws.java` debe renombrarse a `CorsConfig.java`. Java exige que el nombre del archivo coincida exactamente con el nombre de la clase pública.

---

## Conceptos que demuestra este proyecto

| Concepto | Dónde se ve |
|---|---|
| **Contract-first** | `banking.xsd` es la fuente de verdad; las clases Java las genera JAXB en el build |
| **WS-Security (mensaje, no transporte)** | El `<wsse:UsernameToken>` viaja dentro del `<soap:Header>`, no en headers HTTP |
| **SOAP Fault** | Credenciales inválidas, cuenta no encontrada o fondos insuficientes devuelven un Fault estructurado |
| **Separación modelo/contrato** | `Account` (dominio interno) vs. `BalanceResponse` (generado desde XSD) son clases distintas a propósito |
| **CORS en un contexto no-MVC** | Resuelto con un `CorsFilter` de servlet puro con máxima precedencia |

---

## Pendientes / Mejoras planeadas

- [ ] Agregar autorización: verificar que el usuario autenticado sea el dueño de la cuenta que opera
- [ ] Agregar más usuarios WS (`julian`, `carla`) para que cada cuenta tenga credenciales propias
- [ ] Login visual en el frontend con validación real contra el servidor SOAP
- [ ] Manejo de SOAP Faults tipados desde el XSD

---

## Licencia

Proyecto de aprendizaje — sin licencia específica.

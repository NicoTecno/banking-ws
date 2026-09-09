# banking-ws 🏦

Proyecto de aprendizaje de **arquitecturas de API** — implementación de un servicio bancario con **SOAP / contract-first / WS-Security + Autorización por propiedad de cuenta**.

Forma parte de una serie de proyectos que comparan distintos estilos de API (REST, SOAP, GraphQL, gRPC) sobre el mismo dominio de negocio bancario.

---

## Stack

| Capa | Tecnología |
|---|---|
| Backend | Java 24 · Spring Boot 4.1.1 · Spring-WS 5 · WSS4J · JAXB |
| Frontend | React 18 · Vite · Vanilla CSS |
| Protocolo | SOAP 1.1 · WS-Security (UsernameToken / PasswordText) |
| Contrato | Contract-first — las clases Java se generan desde `banking.xsd` |

---

## Estructura del proyecto

```
banking-ws/
├── Backend/
│   └── src/main/
│       ├── java/com/nicolas/bankingws/
│       │   ├── config/
│       │   │   ├── CorsConfig.java           # CORS a nivel de Filter (no MVC)
│       │   │   ├── UserAccountRegistry.java  # Mapa usuario → cuenta (autorización)
│       │   │   ├── WebServiceConfig.java     # MessageDispatcherServlet + WSDL dinámico
│       │   │   └── WsSecurityConfig.java     # Wss4jSecurityInterceptor (UsernameToken)
│       │   ├── endpoint/
│       │   │   └── BankingEndpoint.java      # 3 operaciones + checkAuthorization()
│       │   ├── exception/
│       │   │   ├── AccountNotFoundException.java
│       │   │   ├── InsufficientFundsException.java
│       │   │   └── UnauthorizedException.java  # @SoapFault(CLIENT)
│       │   ├── model/
│       │   │   ├── Account.java
│       │   │   └── Transaction.java
│       │   └── service/
│       │       └── AccountService.java       # Lógica de negocio + datos en memoria
│       └── resources/
│           ├── application.yml
│           └── banking.xsd                   # Contrato — fuente de verdad del API
└── Frontend/
    └── src/
        ├── components/
        │   ├── LoginScreen.jsx   # Login real contra SOAP + XmlViewer del intercambio
        │   ├── BalanceForm.jsx
        │   ├── HistoryForm.jsx
        │   ├── TransferForm.jsx
        │   └── XmlViewer.jsx    # Muestra el XML crudo de request y response
        ├── soap.js              # Cliente SOAP manual (fetch + DOMParser)
        └── App.jsx              # Gestión de sesión: login → terminal
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

## Cuentas y credenciales de demo

Los datos son **en memoria** — se resetean al reiniciar el backend.

Cada usuario tiene una cuenta propia. El servidor verifica que el usuario autenticado solo pueda operar sobre **su propia cuenta** (autorización BOLA/IDOR).

| Usuario | Contraseña | N° de Cuenta | Titular | Saldo inicial |
|---|---|---|---|---|
| `nicolas` | `banking123` | `ACC-1001` | Nicolás Ferreyra | $500,000.00 ARS |
| `julian` | `banking456` | `ACC-1002` | Julián Torres | $120,000.00 ARS |
| `carla` | `banking789` | `ACC-1003` | Carla Gimenez | $75,000.00 ARS |

> **Nota:** Se usa `PasswordDigest` (contraseña en texto plano dentro del XML) para poder probarse fácilmente en Postman. Un sistema real usaría `PasswordDigest` + HTTPS obligatorio.

---

## Flujo de uso (Frontend)

1. **Login** → ingresar usuario y contraseña. El frontend hace un `balanceRequest` contra el servidor SOAP para validar las credenciales. Si el servidor acepta el `<wsse:UsernameToken>`, la sesión se establece. El XML del intercambio se muestra en pantalla para evidenciar cómo viaja la autenticación SOAP.

2. **Terminal bancaria** → una vez logueado, se muestra la cuenta asociada al usuario en el badge de sesión. Todos los formularios tienen la cuenta pre-cargada y bloqueada (readonly). El usuario solo puede operar sobre su cuenta.

3. **Cerrar sesión** → borra el estado local. No hay cookies ni JWT — la "sesión" es solo estado React.

---

## Operaciones disponibles

Todas son `POST http://localhost:8081/ws` con `Content-Type: text/xml`.

### 1. Consultar saldo (`balanceRequest`)

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

### 2. Transferir fondos (`transferRequest`)

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

> ⚠️ Si intentás hacer una transferencia desde una cuenta que no te pertenece, el servidor devuelve un SOAP Fault con `faultCode = CLIENT`.

### 3. Historial de movimientos (`transactionHistoryRequest`)

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

## Errores conocidos resueltos

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

El archivo estaba nombrado `CorsConfig-banking-ws.java`. Java exige que el nombre del archivo coincida exactamente con el nombre de la clase pública.

---

## Conceptos que demuestra este proyecto

| Concepto | Dónde se ve |
|---|---|
| **Contract-first** | `banking.xsd` es la fuente de verdad; las clases Java las genera JAXB en el build |
| **WS-Security — Autenticación** | El `<wsse:UsernameToken>` viaja dentro del `<soap:Header>`, no en headers HTTP | El `<wsse:UsernameToken>` viaja dentro del `<soap:Header>`, no en headers HTTP |
| **WS-Security — Autorización (BOLA/IDOR)** | `BankingEndpoint.checkAuthorization()` extrae el Principal del `WSHandlerConstants.RECV_RESULTS` y verifica que la cuenta operada pertenece al usuario autenticado |
| **SOAP Fault tipado** | `UnauthorizedException`, `AccountNotFoundException` e `InsufficientFundsException` están anotadas con `@SoapFault` para generar faults estructurados |
| **Separación modelo / contrato** | `Account` (dominio interno) vs `BalanceResponse` (generado por JAXB desde XSD) son clases distintas a propósito |
| **CORS en contexto no-MVC** | Resuelto con un `CorsFilter` de servlet puro ya que `WebMvcConfigurer` no aplica en `MessageDispatcherServlet` |
| **Login SOAP visual** | El `LoginScreen` hace un `balanceRequest` real como handshake y muestra el XML para evidenciar cómo viaja la autenticación |

---

## Licencia

Proyecto de aprendizaje — sin licencia específica.


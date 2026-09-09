# Terminal bancaria — frontend de Banking WS

Frontend en React + Vite. **Sin ninguna librería de SOAP ni de XML** —
los tres sobres (`balanceRequest`, `transferRequest`,
`transactionHistoryRequest`) se arman con template strings en
`src/soap.js`, y las respuestas se leen con `DOMParser`, nativo del
navegador. Nada escondido detrás de un cliente generado.

## Por qué se ve así

Encaré esto como una terminal de operaciones de un banco: una carcasa
oscura (la "terminal") con una hoja de papel clara adentro (el
"formulario"), donde cada operación deja un comprobante — así se lee
`result-sheet` en el CSS, literalmente una hoja de resultado con guión
punteado arriba, como un tique. El historial es una tabla tipo libro
mayor. Nada de esto es al azar: SOAP es, en el fondo, más parecido a
llenar formularios de operaciones bancarias que a manipular recursos
(que es la metáfora de REST) o hacer preguntas (la de GraphQL).

## La parte que de verdad importa: ver el XML

Cada operación tiene un botón "Ver el XML que viajó", con el sobre
completo que se mandó y el que volvió, uno al lado del otro. Ninguno de
los otros dos frontends del roadmap mostraba la red cruda — acá sí, a
propósito: la verbosidad del XML (el sobre, los namespaces, el header de
seguridad repetido en cada mensaje) es justamente lo que se señala como
la gran desventaja práctica de SOAP frente a REST/GraphQL. Es más
convincente verlo que leerlo en un README.

## La credencial va en cada mensaje, no en una sesión

La barra de arriba (usuario/contraseña, precargada con
`nicolas`/`banking123`) no "loguea" una vez — esos valores se usan para
armar un `<wsse:UsernameToken>` nuevo en el header de **cada** operación
que mandás. Sacá la contraseña o poné cualquier otra cosa y vas a ver el
fault de autenticación del lado del backend.

## Antes de correrlo: CORS en el backend

Tu backend de SOAP no tenía CORS configurado — no hacía falta hasta
ahora, porque lo estabas probando desde Postman, que no aplica esa
restricción (es cosa del navegador). Te dejo `CorsConfig.java` aparte:
copialo a `banking-ws/src/main/java/com/nicolas/bankingws/config/` y
reiniciá el backend. Ojo, este proyecto no usa
`spring-boot-starter-web`, así que la config no es un
`WebMvcConfigurer` como en los otros dos backends — es un `Filter` de
servlet más de bajo nivel (`CorsFilter`), que además contesta el
`OPTIONS` de preflight que manda el navegador antes del POST real.

## Cómo correrlo

```bash
cd banking-ws-frontend
npm install
npm run dev
```

Abrí `http://localhost:5173`. Necesita el backend corriendo en
`http://localhost:8081` con el `CorsConfig.java` ya agregado.

## Qué probé antes de mandártelo

No pude levantar tu backend acá (mismo motivo que con el proyecto SOAP en
sí: sin salida a Maven Central), así que la verificación fue distinta a
la de REST y GraphQL: armé a mano respuestas SOAP realistas — con el
`ns2:` de prefijo que Spring-WS le pone a los elementos, no el namespace
por defecto que uso en los requests — y corrí las funciones reales de
`soap.js` (no una copia, el archivo tal cual se entrega) contra esos XML:

- Extracción de campos con namespace prefijado (`ns2:ownerName`, etc.) —
  el matching es por namespace-comodín + nombre local, así que no importa
  qué prefijo use el servidor.
- Un acento (`Nicolás`) sobrevive el parseo sin corromperse.
- Detección de `<Fault>` y extracción de `faultstring`.
- Una lista de varias `<transaction>` para el historial, incluyendo una
  sin `counterpartyAccount` (campo opcional en el XSD) — no explota.
- `npm run build` sin errores.

No vi el layout en un navegador real ni lo probé contra el backend
corriendo de verdad — la verificación de la lógica de datos es sólida,
la parte visual queda para cuando lo abras vos.

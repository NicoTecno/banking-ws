const SOAP_URL = import.meta.env.VITE_SOAP_URL || 'http://localhost:8081/ws'
const NS = 'http://bankingws.nicolas.dev/schemas'

function escapeXml(value) {
  return String(value).replace(/[<>&'"]/g, (c) => ({
    '<': '&lt;',
    '>': '&gt;',
    '&': '&amp;',
    "'": '&apos;',
    '"': '&quot;',
  })[c])
}

function securityHeader(username, password) {
  return `<wsse:Security xmlns:wsse="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd" soapenv:mustUnderstand="1">
      <wsse:UsernameToken>
        <wsse:Username>${escapeXml(username)}</wsse:Username>
        <wsse:Password Type="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordText">${escapeXml(password)}</wsse:Password>
      </wsse:UsernameToken>
    </wsse:Security>`
}

function buildEnvelope(bodyXml, credentials) {
  return `<?xml version="1.0" encoding="UTF-8"?>
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/">
  <soapenv:Header>
    ${securityHeader(credentials.username, credentials.password)}
  </soapenv:Header>
  <soapenv:Body>
    ${bodyXml}
  </soapenv:Body>
</soapenv:Envelope>`
}

export function balanceRequestBody(accountNumber) {
  return `<balanceRequest xmlns="${NS}"><accountNumber>${escapeXml(accountNumber)}</accountNumber></balanceRequest>`
}

export function transferRequestBody(fromAccount, toAccount, amount) {
  return `<transferRequest xmlns="${NS}">
      <fromAccount>${escapeXml(fromAccount)}</fromAccount>
      <toAccount>${escapeXml(toAccount)}</toAccount>
      <amount>${Number(amount).toFixed(2)}</amount>
    </transferRequest>`
}

export function transactionHistoryRequestBody(accountNumber) {
  return `<transactionHistoryRequest xmlns="${NS}"><accountNumber>${escapeXml(accountNumber)}</accountNumber></transactionHistoryRequest>`
}

/** Primer elemento con ese nombre local, sin importar el namespace — así no hace falta declarar el default namespace del XSD acá también. */
function textOf(node, localName) {
  return node.getElementsByTagNameNS('*', localName)[0]?.textContent
}

function extractFaultMessage(doc) {
  const fault = doc.getElementsByTagNameNS('*', 'Fault')[0]
  if (!fault) return null
  return textOf(fault, 'faultstring') ?? 'El servidor devolvió un SOAP Fault sin detalle'
}

/**
 * Manda un sobre SOAP completo (con el header de seguridad armado con las
 * credenciales actuales) y devuelve tanto el XML parseado como los dos
 * textos crudos (request y response), para poder mostrarlos tal cual
 * viajaron por la red — esa es la parte más honesta de mostrar SOAP.
 */
export async function callSoap(bodyXml, credentials) {
  const requestXml = buildEnvelope(bodyXml, credentials)

  const response = await fetch(SOAP_URL, {
    method: 'POST',
    headers: { 'Content-Type': 'text/xml; charset=UTF-8' },
    body: requestXml,
  })

  const responseXml = await response.text()
  const doc = new DOMParser().parseFromString(responseXml, 'text/xml')

  const faultMessage = extractFaultMessage(doc)
  if (faultMessage) {
    const error = new Error(faultMessage)
    error.requestXml = requestXml
    error.responseXml = responseXml
    throw error
  }

  return { doc, requestXml, responseXml, textOf: (localName) => textOf(doc, localName) }
}

export function extractTransactions(doc) {
  return Array.from(doc.getElementsByTagNameNS('*', 'transaction')).map((el) => ({
    transactionId: textOf(el, 'transactionId'),
    type: textOf(el, 'type'),
    amount: textOf(el, 'amount'),
    counterpartyAccount: textOf(el, 'counterpartyAccount'),
    timestamp: textOf(el, 'timestamp'),
  }))
}

/** Formateo simple para mostrar el XML legible — no es un parser completo, solo indenta por profundidad de tags. */
export function prettyXml(xml) {
  const withBreaks = xml.replace(/>\s*</g, '>\n<')
  let pad = 0
  return withBreaks
    .split('\n')
    .map((line) => {
      const trimmed = line.trim()
      let indent = pad
      if (/^<\/\w/.test(trimmed)) {
        indent = Math.max(pad - 1, 0)
        pad = indent
      } else if (/^<\w[^>]*[^/]>$/.test(trimmed) && !/^<\?/.test(trimmed)) {
        pad += 1
      }
      return '  '.repeat(indent) + trimmed
    })
    .join('\n')
}

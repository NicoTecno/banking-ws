import { useState } from 'react'
import { callSoap, balanceRequestBody } from '../soap'
import XmlViewer from './XmlViewer'

export default function BalanceForm({ session }) {
  const [result, setResult] = useState(null)
  const [error, setError] = useState(null)
  const [loading, setLoading] = useState(false)
  const [exchange, setExchange] = useState(null)

  async function handleSubmit(event) {
    event.preventDefault()
    setLoading(true)
    setError(null)
    try {
      const { textOf, requestXml, responseXml } = await callSoap(balanceRequestBody(session.account), session.credentials)
      setResult({
        accountNumber: textOf('accountNumber'),
        ownerName: textOf('ownerName'),
        balance: textOf('balance'),
        currency: textOf('currency'),
      })
      setExchange({ requestXml, responseXml })
    } catch (err) {
      setError(err.message)
      setExchange({ requestXml: err.requestXml, responseXml: err.responseXml })
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="operation">
      <h2>Consultar saldo</h2>
      <form onSubmit={handleSubmit} className="operation__form">
        <label>
          Número de cuenta
          <input value={session.account} readOnly />
        </label>
        <button type="submit" disabled={loading}>
          {loading ? 'Consultando…' : 'Consultar'}
        </button>
      </form>

      {error && (
        <div className="operation__error" role="alert">
          {error}
        </div>
      )}

      {result && (
        <dl className="result-sheet">
          <dt>Cuenta</dt>
          <dd className="figure">{result.accountNumber}</dd>
          <dt>Titular</dt>
          <dd>{result.ownerName}</dd>
          <dt>Saldo</dt>
          <dd className="figure figure--large">
            {result.balance} {result.currency}
          </dd>
        </dl>
      )}

      <XmlViewer exchange={exchange} />
    </div>
  )
}

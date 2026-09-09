import { useState } from 'react'
import { callSoap, transferRequestBody } from '../soap'
import XmlViewer from './XmlViewer'

export default function TransferForm({ session }) {
  const [toAccount, setToAccount] = useState('ACC-1002')
  const [amount, setAmount] = useState('5000.00')
  const [result, setResult] = useState(null)
  const [error, setError] = useState(null)
  const [loading, setLoading] = useState(false)
  const [exchange, setExchange] = useState(null)

  async function handleSubmit(event) {
    event.preventDefault()
    setLoading(true)
    setError(null)
    try {
      const { textOf, requestXml, responseXml } = await callSoap(
        transferRequestBody(session.account, toAccount, amount),
        session.credentials
      )
      setResult({
        transactionId: textOf('transactionId'),
        status: textOf('status'),
        newBalance: textOf('fromAccountNewBalance'),
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
      <h2>Transferir</h2>
      <form onSubmit={handleSubmit} className="operation__form operation__form--grid">
        <label>
          Hacia
          <input value={toAccount} onChange={(e) => setToAccount(e.target.value)} required />
        </label>
        <label>
          Monto
          <input type="number" min="0.01" step="0.01" value={amount} onChange={(e) => setAmount(e.target.value)} required />
        </label>
        <button type="submit" disabled={loading}>
          {loading ? 'Transfiriendo…' : 'Transferir'}
        </button>
      </form>

      {error && (
        <div className="operation__error" role="alert">
          {error}
        </div>
      )}

      {result && (
        <dl className="result-sheet">
          <dt>Estado</dt>
          <dd className="status-chip">{result.status}</dd>
          <dt>ID de transacción</dt>
          <dd className="figure">{result.transactionId}</dd>
          <dt>Nuevo saldo de origen</dt>
          <dd className="figure figure--large">{result.newBalance}</dd>
        </dl>
      )}

      <XmlViewer exchange={exchange} />
    </div>
  )
}

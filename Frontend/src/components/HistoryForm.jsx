import { useState } from 'react'
import { callSoap, transactionHistoryRequestBody, extractTransactions } from '../soap'
import XmlViewer from './XmlViewer'

const TYPE_LABELS = { DEBIT: 'Débito', CREDIT: 'Crédito' }

export default function HistoryForm({ session }) {
  const [transactions, setTransactions] = useState(null)
  const [error, setError] = useState(null)
  const [loading, setLoading] = useState(false)
  const [exchange, setExchange] = useState(null)

  async function handleSubmit(event) {
    event.preventDefault()
    setLoading(true)
    setError(null)
    try {
      const { doc, requestXml, responseXml } = await callSoap(
        transactionHistoryRequestBody(session.account),
        session.credentials
      )
      setTransactions(extractTransactions(doc))
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
      <h2>Historial de movimientos</h2>
      <form onSubmit={handleSubmit} className="operation__form">
        <label>
          Número de cuenta
          <input value={session.account} readOnly />
        </label>
        <button type="submit" disabled={loading}>
          {loading ? 'Buscando…' : 'Ver historial'}
        </button>
      </form>

      {error && (
        <div className="operation__error" role="alert">
          {error}
        </div>
      )}

      {transactions && (
        <table className="ledger">
          <thead>
            <tr>
              <th>Tipo</th>
              <th>Monto</th>
              <th>Contraparte</th>
              <th>Fecha</th>
            </tr>
          </thead>
          <tbody>
            {transactions.length === 0 && (
              <tr>
                <td colSpan={4} className="ledger__empty">
                  Sin movimientos todavía.
                </td>
              </tr>
            )}
            {transactions.map((tx) => (
              <tr key={tx.transactionId}>
                <td className={`ledger__type ledger__type--${tx.type?.toLowerCase()}`}>
                  {TYPE_LABELS[tx.type] ?? tx.type}
                </td>
                <td className="figure">{tx.amount}</td>
                <td className="figure">{tx.counterpartyAccount ?? '—'}</td>
                <td>{tx.timestamp ? new Date(tx.timestamp).toLocaleString('es-AR') : '—'}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}

      <XmlViewer exchange={exchange} />
    </div>
  )
}

import { useState } from 'react'
import CredentialsBar from './components/CredentialsBar'
import BalanceForm from './components/BalanceForm'
import TransferForm from './components/TransferForm'
import HistoryForm from './components/HistoryForm'
import './App.css'

const TABS = [
  { id: 'balance', label: 'Saldo' },
  { id: 'transfer', label: 'Transferir' },
  { id: 'history', label: 'Historial' },
]

export default function App() {
  const [tab, setTab] = useState('balance')
  const [credentials, setCredentials] = useState({ username: 'nicolas', password: 'banking123' })

  return (
    <div className="terminal">
      <header className="terminal__header">
        <h1>Terminal bancaria</h1>
        <p className="terminal__subtitle">Banking WS · SOAP</p>
      </header>

      <CredentialsBar credentials={credentials} onChange={setCredentials} />

      <nav className="terminal__tabs" role="tablist">
        {TABS.map((t) => (
          <button
            key={t.id}
            role="tab"
            aria-selected={tab === t.id}
            className={tab === t.id ? 'is-active' : ''}
            onClick={() => setTab(t.id)}
          >
            {t.label}
          </button>
        ))}
      </nav>

      <main className="terminal__window">
        {tab === 'balance' && <BalanceForm credentials={credentials} />}
        {tab === 'transfer' && <TransferForm credentials={credentials} />}
        {tab === 'history' && <HistoryForm credentials={credentials} />}
      </main>
    </div>
  )
}

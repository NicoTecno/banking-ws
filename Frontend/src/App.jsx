import { useState } from 'react'
import LoginScreen from './components/LoginScreen'
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
  const [session, setSession] = useState(null)
  const [tab, setTab] = useState('balance')

  if (!session) {
    return <LoginScreen onLogin={setSession} />
  }

  return (
    <div className="terminal">
      <header className="terminal__header">
        <div className="terminal__header-top">
          <div>
            <h1>Terminal bancaria</h1>
            <p className="terminal__subtitle">Banking WS · SOAP</p>
          </div>
          <div className="session-badge">
            <div className="session-badge__info">
              <span className="session-badge__name">{session.name}</span>
              <span className="session-badge__account">{session.account}</span>
            </div>
            <button className="session-badge__logout" onClick={() => setSession(null)}>
              Cerrar sesión
            </button>
          </div>
        </div>
      </header>

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
        {tab === 'balance' && <BalanceForm session={session} />}
        {tab === 'transfer' && <TransferForm session={session} />}
        {tab === 'history' && <HistoryForm session={session} />}
      </main>
    </div>
  )
}


import { useState } from 'react'
import { callSoap, balanceRequestBody, USER_ACCOUNTS } from '../soap'
import XmlViewer from './XmlViewer'

export default function LoginScreen({ onLogin }) {
  const [username, setUsername] = useState('nicolas')
  const [password, setPassword] = useState('banking123')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)
  const [exchange, setExchange] = useState(null)

  async function handleLogin(e) {
    e.preventDefault()
    setLoading(true)
    setError(null)
    
    const credentials = { username, password }
    const accountInfo = USER_ACCOUNTS[username]
    
    if (!accountInfo) {
      setError(`Usuario '${username}' no reconocido en el frontend (solo nicolas, julian, carla).`)
      setLoading(false)
      return
    }

    try {
      // Usamos balanceRequest de su propia cuenta como "ping" para validar credenciales
      const { requestXml, responseXml } = await callSoap(balanceRequestBody(accountInfo.account), credentials)
      setExchange({ requestXml, responseXml })
      
      // Si no tiró error, las credenciales son válidas
      onLogin({
        credentials,
        account: accountInfo.account,
        name: accountInfo.name
      })
    } catch (err) {
      setError(err.message)
      setExchange({ requestXml: err.requestXml, responseXml: err.responseXml })
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="login-screen">
      <div className="login-card">
        <header className="login-card__header">
          <span className="login-card__seal" aria-hidden="true">✦</span>
          <h2>Terminal bancaria</h2>
          <p>Autenticación WS-Security</p>
        </header>

        <form onSubmit={handleLogin} className="login-card__form">
          <label>
            Usuario
            <input 
              value={username} 
              onChange={(e) => setUsername(e.target.value)} 
              required 
            />
          </label>
          <label>
            Contraseña
            <input 
              type="password" 
              value={password} 
              onChange={(e) => setPassword(e.target.value)} 
              required 
            />
          </label>
          <button type="submit" disabled={loading}>
            {loading ? 'Validando…' : 'Iniciar sesión'}
          </button>
        </form>

        {error && (
          <div className="operation__error" role="alert" style={{ marginTop: '16px', marginBottom: 0 }}>
            {error}
          </div>
        )}

        <div className="login-card__hint">
          <strong>Cuentas de prueba:</strong>
          <ul>
            <li><code>nicolas</code> / <code>banking123</code> (ACC-1001)</li>
            <li><code>julian</code> / <code>banking456</code> (ACC-1002)</li>
            <li><code>carla</code> / <code>banking789</code> (ACC-1003)</li>
          </ul>
        </div>
      </div>
      
      {exchange && (
        <div className="login-xml">
          <p>El login en SOAP no es una sesión, sino que la credencial viaja en cada mensaje. Arriba hicimos un <code>balanceRequest</code> de prueba. Mirá el <code>&lt;wsse:UsernameToken&gt;</code> en el request:</p>
          <XmlViewer exchange={exchange} />
        </div>
      )}
    </div>
  )
}

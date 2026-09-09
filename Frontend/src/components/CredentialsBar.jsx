export default function CredentialsBar({ credentials, onChange }) {
  return (
    <div className="credentials-bar">
      <span className="credentials-bar__seal" aria-hidden="true">
        ✦
      </span>
      <div className="credentials-bar__fields">
        <label>
          Usuario
          <input
            value={credentials.username}
            onChange={(e) => onChange({ ...credentials, username: e.target.value })}
          />
        </label>
        <label>
          Contraseña
          <input
            type="password"
            value={credentials.password}
            onChange={(e) => onChange({ ...credentials, password: e.target.value })}
          />
        </label>
      </div>
      <p className="credentials-bar__note">
        Estas credenciales van dentro del <code>&lt;wsse:UsernameToken&gt;</code>, en cada operación
        — no hay sesión ni token que se reutilice, cada mensaje se autentica solo.
      </p>
    </div>
  )
}

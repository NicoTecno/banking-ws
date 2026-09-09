import { useState } from 'react'
import { prettyXml } from '../soap'

export default function XmlViewer({ exchange }) {
  const [open, setOpen] = useState(false)

  if (!exchange) return null

  return (
    <div className="xml-viewer">
      <button type="button" className="xml-viewer__toggle" onClick={() => setOpen((v) => !v)}>
        {open ? 'Ocultar' : 'Ver'} el XML que viajó
      </button>
      {open && (
        <div className="xml-viewer__panels">
          <div>
            <h3>Enviado</h3>
            <pre>{prettyXml(exchange.requestXml)}</pre>
          </div>
          <div>
            <h3>Recibido</h3>
            <pre>{prettyXml(exchange.responseXml)}</pre>
          </div>
        </div>
      )}
    </div>
  )
}

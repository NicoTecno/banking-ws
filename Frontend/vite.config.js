import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  // Mismo puerto que los otros frontends del roadmap. El backend de SOAP
  // no tenía CORS configurado todavía (no hizo falta hasta ahora) — te
  // dejo el archivo nuevo aparte, igual que con REST.
  server: {
    port: 5173,
    strictPort: true,
  },
})

'use client'

import { useState, useRef, useEffect, useCallback } from 'react'
import { useRouter, useSearchParams } from 'next/navigation'
import Link from 'next/link'
import { api } from '@/lib/api'

function OtpInput({
  digits,
  onChange,
  onKeyDown,
  onPaste,
  inputRefs,
  hasError,
}: {
  digits: string[]
  onChange: (index: number, value: string) => void
  onKeyDown: (index: number, e: React.KeyboardEvent<HTMLInputElement>) => void
  onPaste: (e: React.ClipboardEvent) => void
  inputRefs: React.MutableRefObject<(HTMLInputElement | null)[]>
  hasError: boolean
}) {
  return (
    <div className="flex gap-2 justify-center" onPaste={onPaste}>
      {digits.map((digit, i) => (
        <input
          key={i}
          ref={el => { inputRefs.current[i] = el }}
          type="text"
          inputMode="numeric"
          maxLength={1}
          value={digit}
          onChange={e => onChange(i, e.target.value)}
          onKeyDown={e => onKeyDown(i, e)}
          className={`h-12 w-12 rounded-lg border text-center text-lg font-semibold outline-none transition focus:ring-2 focus:ring-blue-500 ${
            hasError ? 'border-red-400 bg-red-50' : 'border-gray-300 bg-white'
          }`}
        />
      ))}
    </div>
  )
}

export default function VerificarEmailPage() {
  const router = useRouter()
  const searchParams = useSearchParams()
  const email = searchParams.get('email') ?? ''

  const [digits, setDigits] = useState<string[]>(Array(6).fill(''))
  const [error, setError] = useState('')
  const [success, setSuccess] = useState(false)
  const [loading, setLoading] = useState(false)
  const inputRefs = useRef<(HTMLInputElement | null)[]>([])

  useEffect(() => {
    inputRefs.current[0]?.focus()
  }, [])

  const handleDigitChange = useCallback((index: number, value: string) => {
    const digit = value.replace(/\D/g, '').slice(-1)
    setDigits(prev => {
      const next = [...prev]
      next[index] = digit
      return next
    })
    setError('')
    if (digit && index < 5) {
      inputRefs.current[index + 1]?.focus()
    }
  }, [])

  function handleKeyDown(index: number, e: React.KeyboardEvent<HTMLInputElement>) {
    if (e.key === 'Backspace' && !digits[index] && index > 0) {
      inputRefs.current[index - 1]?.focus()
    }
  }

  function handlePaste(e: React.ClipboardEvent) {
    e.preventDefault()
    const pasted = e.clipboardData.getData('text').replace(/\D/g, '').slice(0, 6)
    if (pasted.length > 0) {
      const next = Array(6).fill('')
      for (let i = 0; i < pasted.length; i++) next[i] = pasted[i]
      setDigits(next)
      inputRefs.current[Math.min(pasted.length, 5)]?.focus()
    }
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    const code = digits.join('')
    if (code.length < 6) {
      setError('Ingresa los 6 dígitos del código')
      return
    }

    setLoading(true)
    try {
      await api.post('/auth/verify-email', { email, code })
      setSuccess(true)
      setTimeout(() => router.push('/login'), 2000)
    } catch (err: unknown) {
      const apiError = err as { status?: number }
      if (apiError.status === 400) {
        setError('El código no es válido o ha expirado.')
      } else if (apiError.status === 429) {
        setError('Demasiados intentos. Espera unos minutos e intenta de nuevo.')
      } else {
        setError('Ocurrió un error. Intenta de nuevo más tarde.')
      }
      setDigits(Array(6).fill(''))
      inputRefs.current[0]?.focus()
    } finally {
      setLoading(false)
    }
  }

  if (!email) {
    return (
      <main className="min-h-screen flex items-center justify-center bg-gray-50 px-4">
        <div className="text-center">
          <p className="text-gray-600 mb-4">No se encontró el correo electrónico.</p>
          <Link href="/registro" className="text-blue-600 hover:underline text-sm">
            Volver al registro
          </Link>
        </div>
      </main>
    )
  }

  if (success) {
    return (
      <main className="min-h-screen flex items-center justify-center bg-gray-50 px-4">
        <div className="w-full max-w-md bg-white rounded-2xl shadow-sm border border-gray-100 p-8 text-center">
          <div className="mx-auto mb-4 flex h-12 w-12 items-center justify-center rounded-full bg-green-100">
            <svg className="h-6 w-6 text-green-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
            </svg>
          </div>
          <h1 className="text-xl font-bold text-gray-900 mb-2">Correo verificado</h1>
          <p className="text-sm text-gray-500">Redirigiendo al inicio de sesión...</p>
        </div>
      </main>
    )
  }

  return (
    <main className="min-h-screen flex items-center justify-center bg-gray-50 px-4 py-12">
      <div className="w-full max-w-md bg-white rounded-2xl shadow-sm border border-gray-100 p-8">
        <h1 className="text-2xl font-bold text-gray-900 mb-2">Verifica tu correo</h1>
        <p className="text-sm text-gray-500 mb-6">
          Enviamos un código de 6 dígitos a{' '}
          <span className="font-medium text-gray-700">{email}</span>
        </p>

        <form onSubmit={handleSubmit} className="space-y-6">
          <div>
            <OtpInput
              digits={digits}
              onChange={handleDigitChange}
              onKeyDown={handleKeyDown}
              onPaste={handlePaste}
              inputRefs={inputRefs}
              hasError={!!error}
            />
            {error && (
              <p className="mt-3 text-center text-sm text-red-600">{error}</p>
            )}
          </div>

          <button
            type="submit"
            disabled={loading || digits.join('').length < 6}
            className="w-full rounded-lg bg-blue-600 px-4 py-2.5 text-sm font-semibold text-white transition hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            {loading ? 'Verificando...' : 'Verificar cuenta'}
          </button>
        </form>

        <p className="mt-6 text-center text-sm text-gray-500">
          <Link href="/registro" className="font-medium text-blue-600 hover:underline">
            Volver al registro
          </Link>
        </p>
      </div>
    </main>
  )
}

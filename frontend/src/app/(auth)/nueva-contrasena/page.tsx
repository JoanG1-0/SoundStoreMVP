'use client'

import { useState, useRef, useEffect, useCallback } from 'react'
import { useRouter, useSearchParams } from 'next/navigation'
import Link from 'next/link'
import { api } from '@/lib/api'

interface FormErrors {
  password?: string
  confirmPassword?: string
}

function validate(password: string, confirmPassword: string): FormErrors {
  const errors: FormErrors = {}
  if (!password) {
    errors.password = 'La contraseña es obligatoria'
  } else if (password.length < 8) {
    errors.password = 'La contraseña debe tener al menos 8 caracteres'
  }
  if (!confirmPassword) {
    errors.confirmPassword = 'Confirma tu nueva contraseña'
  } else if (password !== confirmPassword) {
    errors.confirmPassword = 'Las contraseñas no coinciden'
  }
  return errors
}

export default function NuevaContrasenaPage() {
  const router = useRouter()
  const searchParams = useSearchParams()
  const email = searchParams.get('email') ?? ''

  const [digits, setDigits] = useState<string[]>(Array(6).fill(''))
  const [password, setPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [otpError, setOtpError] = useState('')
  const [formErrors, setFormErrors] = useState<FormErrors>({})
  const [serverError, setServerError] = useState('')
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
    setOtpError('')
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
    setServerError('')

    const code = digits.join('')
    if (code.length < 6) {
      setOtpError('Ingresa los 6 dígitos del código')
      return
    }

    const errors = validate(password, confirmPassword)
    if (Object.keys(errors).length > 0) {
      setFormErrors(errors)
      return
    }

    setLoading(true)
    try {
      await api.post('/auth/reset-password', {
        email,
        otpCode: code,
        newPassword: password,
      })
      setSuccess(true)
      setTimeout(() => router.push('/login'), 2500)
    } catch (err: unknown) {
      const error = err as { status?: number }
      if (error.status === 400) {
        setOtpError('El código no es válido o ha expirado.')
        setDigits(Array(6).fill(''))
        inputRefs.current[0]?.focus()
      } else if (error.status === 429) {
        setServerError('Demasiados intentos. Espera unos minutos e intenta de nuevo.')
      } else {
        setServerError('Ocurrió un error. Intenta de nuevo más tarde.')
      }
    } finally {
      setLoading(false)
    }
  }

  if (!email) {
    return (
      <main className="min-h-screen flex items-center justify-center bg-gray-50 px-4">
        <div className="text-center">
          <p className="text-gray-600 mb-4">Enlace inválido. Solicita un nuevo código.</p>
          <Link href="/recuperar-contrasena" className="text-blue-600 hover:underline text-sm">
            Recuperar contraseña
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
          <h1 className="text-xl font-bold text-gray-900 mb-2">Contraseña actualizada</h1>
          <p className="text-sm text-gray-500">Redirigiendo al inicio de sesión...</p>
        </div>
      </main>
    )
  }

  return (
    <main className="min-h-screen flex items-center justify-center bg-gray-50 px-4 py-12">
      <div className="w-full max-w-md bg-white rounded-2xl shadow-sm border border-gray-100 p-8">
        <h1 className="text-2xl font-bold text-gray-900 mb-2">Nueva contraseña</h1>
        <p className="text-sm text-gray-500 mb-6">
          Ingresa el código enviado a{' '}
          <span className="font-medium text-gray-700">{email}</span>{' '}
          y elige una nueva contraseña.
        </p>

        <form onSubmit={handleSubmit} className="space-y-5">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Código de verificación
            </label>
            <div className="flex gap-2 justify-center" onPaste={handlePaste}>
              {digits.map((digit, i) => (
                <input
                  key={i}
                  ref={el => { inputRefs.current[i] = el }}
                  type="text"
                  inputMode="numeric"
                  maxLength={1}
                  value={digit}
                  onChange={e => handleDigitChange(i, e.target.value)}
                  onKeyDown={e => handleKeyDown(i, e)}
                  className={`h-12 w-12 rounded-lg border text-center text-lg font-semibold outline-none transition focus:ring-2 focus:ring-blue-500 ${
                    otpError ? 'border-red-400 bg-red-50' : 'border-gray-300 bg-white'
                  }`}
                />
              ))}
            </div>
            {otpError && (
              <p className="mt-2 text-center text-sm text-red-600">{otpError}</p>
            )}
          </div>

          <div>
            <label htmlFor="password" className="block text-sm font-medium text-gray-700 mb-1">
              Nueva contraseña
            </label>
            <input
              id="password"
              type="password"
              autoComplete="new-password"
              value={password}
              onChange={e => {
                setPassword(e.target.value)
                setFormErrors(prev => ({ ...prev, password: undefined }))
              }}
              placeholder="Mínimo 8 caracteres"
              className={`w-full rounded-lg border px-3 py-2 text-sm outline-none transition focus:ring-2 focus:ring-blue-500 ${
                formErrors.password ? 'border-red-400 bg-red-50' : 'border-gray-300 bg-white'
              }`}
            />
            {formErrors.password && (
              <p className="mt-1 text-xs text-red-600">{formErrors.password}</p>
            )}
          </div>

          <div>
            <label htmlFor="confirmPassword" className="block text-sm font-medium text-gray-700 mb-1">
              Confirmar contraseña
            </label>
            <input
              id="confirmPassword"
              type="password"
              autoComplete="new-password"
              value={confirmPassword}
              onChange={e => {
                setConfirmPassword(e.target.value)
                setFormErrors(prev => ({ ...prev, confirmPassword: undefined }))
              }}
              placeholder="Repite tu contraseña"
              className={`w-full rounded-lg border px-3 py-2 text-sm outline-none transition focus:ring-2 focus:ring-blue-500 ${
                formErrors.confirmPassword ? 'border-red-400 bg-red-50' : 'border-gray-300 bg-white'
              }`}
            />
            {formErrors.confirmPassword && (
              <p className="mt-1 text-xs text-red-600">{formErrors.confirmPassword}</p>
            )}
          </div>

          {serverError && (
            <p className="rounded-lg bg-red-50 border border-red-200 px-3 py-2 text-sm text-red-600">
              {serverError}
            </p>
          )}

          <button
            type="submit"
            disabled={loading}
            className="w-full rounded-lg bg-blue-600 px-4 py-2.5 text-sm font-semibold text-white transition hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            {loading ? 'Guardando...' : 'Cambiar contraseña'}
          </button>
        </form>

        <p className="mt-6 text-center text-sm text-gray-500">
          <Link href="/login" className="font-medium text-blue-600 hover:underline">
            Volver al inicio de sesión
          </Link>
        </p>
      </div>
    </main>
  )
}

'use client'

import { useState } from 'react'
import { useRouter } from 'next/navigation'
import Link from 'next/link'
import { api } from '@/lib/api'

interface LoginResponseDto {
  accessToken: string
  refreshToken: string
  role: string
}

interface FormFields {
  email: string
  password: string
}

interface FormErrors {
  email?: string
  password?: string
}

function validate(data: FormFields): FormErrors {
  const errors: FormErrors = {}
  if (!data.email.trim()) {
    errors.email = 'El correo electrónico es obligatorio'
  } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(data.email)) {
    errors.email = 'Ingresa un correo electrónico válido'
  }
  if (!data.password) {
    errors.password = 'La contraseña es obligatoria'
  }
  return errors
}

export default function LoginPage() {
  const router = useRouter()
  const [form, setForm] = useState<FormFields>({ email: '', password: '' })
  const [errors, setErrors] = useState<FormErrors>({})
  const [serverError, setServerError] = useState('')
  const [loading, setLoading] = useState(false)

  function handleChange(e: React.ChangeEvent<HTMLInputElement>) {
    const { name, value } = e.target
    setForm(prev => ({ ...prev, [name]: value }))
    if (errors[name as keyof FormErrors]) {
      setErrors(prev => ({ ...prev, [name]: undefined }))
    }
    setServerError('')
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    setServerError('')

    const validationErrors = validate(form)
    if (Object.keys(validationErrors).length > 0) {
      setErrors(validationErrors)
      return
    }

    setLoading(true)
    try {
      const response = await api.post<LoginResponseDto>('/auth/login', {
        email: form.email.trim().toLowerCase(),
        password: form.password,
      })
      localStorage.setItem('accessToken', response.accessToken)
      localStorage.setItem('refreshToken', response.refreshToken)
      localStorage.setItem('role', response.role)
      router.push('/')
    } catch (err: unknown) {
      const error = err as { status?: number }
      if (error.status === 401) {
        setServerError('Correo o contraseña incorrectos.')
      } else {
        setServerError('Ocurrió un error. Intenta de nuevo más tarde.')
      }
    } finally {
      setLoading(false)
    }
  }

  return (
    <main className="min-h-screen flex items-center justify-center bg-gray-50 px-4 py-12">
      <div className="w-full max-w-md bg-white rounded-2xl shadow-sm border border-gray-100 p-8">
        <h1 className="text-2xl font-bold text-gray-900 mb-2">Iniciar sesión</h1>
        <p className="text-sm text-gray-500 mb-6">Bienvenido de vuelta a SoundStore</p>

        <form onSubmit={handleSubmit} noValidate className="space-y-4">
          <div>
            <label htmlFor="email" className="block text-sm font-medium text-gray-700 mb-1">
              Correo electrónico
            </label>
            <input
              id="email"
              name="email"
              type="email"
              autoComplete="email"
              value={form.email}
              onChange={handleChange}
              placeholder="juan@ejemplo.com"
              className={`w-full rounded-lg border px-3 py-2 text-sm outline-none transition focus:ring-2 focus:ring-blue-500 ${
                errors.email ? 'border-red-400 bg-red-50' : 'border-gray-300 bg-white'
              }`}
            />
            {errors.email && (
              <p className="mt-1 text-xs text-red-600">{errors.email}</p>
            )}
          </div>

          <div>
            <div className="flex items-center justify-between mb-1">
              <label htmlFor="password" className="block text-sm font-medium text-gray-700">
                Contraseña
              </label>
              <Link href="/recuperar-contrasena" className="text-xs text-blue-600 hover:underline">
                ¿Olvidaste tu contraseña?
              </Link>
            </div>
            <input
              id="password"
              name="password"
              type="password"
              autoComplete="current-password"
              value={form.password}
              onChange={handleChange}
              placeholder="Tu contraseña"
              className={`w-full rounded-lg border px-3 py-2 text-sm outline-none transition focus:ring-2 focus:ring-blue-500 ${
                errors.password ? 'border-red-400 bg-red-50' : 'border-gray-300 bg-white'
              }`}
            />
            {errors.password && (
              <p className="mt-1 text-xs text-red-600">{errors.password}</p>
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
            {loading ? 'Ingresando...' : 'Iniciar sesión'}
          </button>
        </form>

        <p className="mt-6 text-center text-sm text-gray-500">
          ¿No tienes una cuenta?{' '}
          <Link href="/registro" className="font-medium text-blue-600 hover:underline">
            Regístrate
          </Link>
        </p>
      </div>
    </main>
  )
}

'use client';

import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import { useAuth } from '@/context/AuthContext';
import { api } from '@/lib/api';

interface UserProfile {
  id: string;
  fullName: string;
  email: string;
  phone: string;
  address: string | null;
  role: string;
}

interface FormFields {
  fullName: string;
  phone: string;
  address: string;
}

interface FormErrors {
  fullName?: string;
  phone?: string;
}

function validate(data: FormFields): FormErrors {
  const errors: FormErrors = {};
  if (!data.fullName.trim()) errors.fullName = 'El nombre es obligatorio';
  if (!data.phone.trim()) errors.phone = 'El teléfono es obligatorio';
  return errors;
}

interface PasswordForm {
  currentPassword: string;
  newPassword: string;
}

export default function PerfilPage() {
  const router = useRouter();
  const { isAuthenticated, getToken } = useAuth();
  const [perfil, setPerfil] = useState<UserProfile | null>(null);
  const [editando, setEditando] = useState(false);
  const [form, setForm] = useState<FormFields>({ fullName: '', phone: '', address: '' });
  const [errors, setErrors] = useState<FormErrors>({});
  const [serverError, setServerError] = useState('');
  const [guardando, setGuardando] = useState(false);
  const [exito, setExito] = useState(false);
  const [cargando, setCargando] = useState(true);

  const [pwForm, setPwForm] = useState<PasswordForm>({ currentPassword: '', newPassword: '' });
  const [pwError, setPwError] = useState('');
  const [pwExito, setPwExito] = useState(false);
  const [pwGuardando, setPwGuardando] = useState(false);

  useEffect(() => {
    if (!isAuthenticated) {
      router.push('/login');
      return;
    }
    const token = getToken();
    if (!token) return;

    api.get<UserProfile>('/users/me', token)
      .then((data) => {
        setPerfil(data);
        setForm({ fullName: data.fullName, phone: data.phone, address: data.address ?? '' });
      })
      .catch(() => setServerError('No se pudo cargar tu perfil. Intenta de nuevo.'))
      .finally(() => setCargando(false));
  }, [isAuthenticated, getToken, router]);

  function handleChange(e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) {
    const { name, value } = e.target;
    setForm(prev => ({ ...prev, [name]: value }));
    if (errors[name as keyof FormErrors]) {
      setErrors(prev => ({ ...prev, [name]: undefined }));
    }
    setServerError('');
    setExito(false);
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    const validationErrors = validate(form);
    if (Object.keys(validationErrors).length > 0) {
      setErrors(validationErrors);
      return;
    }
    const token = getToken();
    if (!token) return;

    setGuardando(true);
    setServerError('');
    try {
      const updated = await api.put<UserProfile>('/users/me', {
        fullName: form.fullName.trim(),
        phone: form.phone.trim(),
        address: form.address.trim() || null,
      }, token);
      setPerfil(updated);
      setForm({ fullName: updated.fullName, phone: updated.phone, address: updated.address ?? '' });
      setEditando(false);
      setExito(true);
      setTimeout(() => setExito(false), 3000);
    } catch {
      setServerError('No se pudieron guardar los cambios. Intenta de nuevo.');
    } finally {
      setGuardando(false);
    }
  }

  async function handlePasswordChange(e: React.FormEvent) {
    e.preventDefault();
    if (!pwForm.currentPassword || !pwForm.newPassword) {
      setPwError('Completa ambos campos');
      return;
    }
    if (pwForm.newPassword.length < 8) {
      setPwError('La nueva contraseña debe tener al menos 8 caracteres');
      return;
    }
    const token = getToken();
    if (!token) return;
    setPwGuardando(true);
    setPwError('');
    try {
      await api.patch('/users/me/password', {
        currentPassword: pwForm.currentPassword,
        newPassword: pwForm.newPassword,
      }, token);
      setPwForm({ currentPassword: '', newPassword: '' });
      setPwExito(true);
      setTimeout(() => setPwExito(false), 3000);
    } catch (err: unknown) {
      const e = err as { message?: string };
      setPwError(e?.message ?? 'No se pudo cambiar la contraseña');
    } finally {
      setPwGuardando(false);
    }
  }

  if (cargando) {
    return (
      <main className="min-h-screen bg-gray-50 flex items-center justify-center">
        <p className="text-gray-400 text-sm">Cargando perfil...</p>
      </main>
    );
  }

  return (
    <main className="min-h-screen bg-gray-50">
      <div className="max-w-2xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="mb-6">
          <h1 className="text-2xl font-bold text-gray-900">Mi perfil</h1>
          <p className="text-gray-500 text-sm mt-1">Gestiona tus datos personales</p>
        </div>

        {exito && (
          <div className="bg-green-50 border border-green-200 rounded-xl px-4 py-3 mb-4 text-sm text-green-700">
            Tus datos han sido actualizados correctamente.
          </div>
        )}

        {serverError && (
          <div className="bg-red-50 border border-red-200 rounded-xl px-4 py-3 mb-4 text-sm text-red-600">
            {serverError}
          </div>
        )}

        <div className="bg-white rounded-2xl border border-gray-100 shadow-sm p-6 space-y-5">
          <div className="flex items-center justify-between">
            <h2 className="font-semibold text-gray-900">Datos personales</h2>
            {!editando && (
              <button
                onClick={() => setEditando(true)}
                className="text-sm text-indigo-600 hover:text-indigo-700 font-medium transition-colors"
              >
                Editar
              </button>
            )}
          </div>

          {!editando ? (
            <div className="space-y-4">
              {[
                { label: 'Nombre completo', value: perfil?.fullName },
                { label: 'Correo electrónico', value: perfil?.email },
                { label: 'Teléfono', value: perfil?.phone },
                { label: 'Dirección de entrega', value: perfil?.address ?? 'No registrada' },
              ].map(({ label, value }) => (
                <div key={label}>
                  <p className="text-xs text-gray-400 font-medium uppercase tracking-wide">{label}</p>
                  <p className="text-gray-700 mt-0.5">{value || '—'}</p>
                </div>
              ))}
            </div>
          ) : (
            <form onSubmit={handleSubmit} noValidate className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Nombre completo</label>
                <input
                  type="text"
                  name="fullName"
                  value={form.fullName}
                  onChange={handleChange}
                  className={`w-full px-3 py-2 border rounded-lg text-sm outline-none focus:ring-2 focus:ring-indigo-500 transition ${
                    errors.fullName ? 'border-red-400 bg-red-50' : 'border-gray-300'
                  }`}
                />
                {errors.fullName && <p className="mt-1 text-xs text-red-600">{errors.fullName}</p>}
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Teléfono</label>
                <input
                  type="text"
                  name="phone"
                  value={form.phone}
                  onChange={handleChange}
                  className={`w-full px-3 py-2 border rounded-lg text-sm outline-none focus:ring-2 focus:ring-indigo-500 transition ${
                    errors.phone ? 'border-red-400 bg-red-50' : 'border-gray-300'
                  }`}
                />
                {errors.phone && <p className="mt-1 text-xs text-red-600">{errors.phone}</p>}
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Dirección de entrega</label>
                <textarea
                  name="address"
                  value={form.address}
                  onChange={handleChange}
                  rows={3}
                  placeholder="Opcional"
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm outline-none focus:ring-2 focus:ring-indigo-500 transition resize-none"
                />
              </div>
              <div className="flex gap-3">
                <button
                  type="submit"
                  disabled={guardando}
                  className="flex-1 py-2.5 rounded-xl bg-indigo-600 hover:bg-indigo-700 text-white text-sm font-semibold transition disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  {guardando ? 'Guardando...' : 'Guardar cambios'}
                </button>
                <button
                  type="button"
                  onClick={() => {
                    setEditando(false);
                    setErrors({});
                    setServerError('');
                    setForm({ fullName: perfil?.fullName ?? '', phone: perfil?.phone ?? '', address: perfil?.address ?? '' });
                  }}
                  className="px-4 py-2.5 rounded-xl border border-gray-300 text-sm text-gray-600 hover:bg-gray-50 transition"
                >
                  Cancelar
                </button>
              </div>
            </form>
          )}
        </div>

        {/* Cambiar contraseña */}
        <div className="bg-white rounded-2xl border border-gray-100 shadow-sm p-6 space-y-4 mt-4">
          <h2 className="font-semibold text-gray-900">Cambiar contraseña</h2>

          {pwExito && (
            <div className="bg-green-50 border border-green-200 rounded-xl px-4 py-3 text-sm text-green-700">
              Contraseña actualizada correctamente.
            </div>
          )}
          {pwError && (
            <div className="bg-red-50 border border-red-200 rounded-xl px-4 py-3 text-sm text-red-600">
              {pwError}
            </div>
          )}

          <form onSubmit={handlePasswordChange} noValidate className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Contraseña actual</label>
              <input
                type="password"
                value={pwForm.currentPassword}
                onChange={e => { setPwForm(f => ({ ...f, currentPassword: e.target.value })); setPwError(''); }}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm outline-none focus:ring-2 focus:ring-indigo-500 transition"
                autoComplete="current-password"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Nueva contraseña</label>
              <input
                type="password"
                value={pwForm.newPassword}
                onChange={e => { setPwForm(f => ({ ...f, newPassword: e.target.value })); setPwError(''); }}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm outline-none focus:ring-2 focus:ring-indigo-500 transition"
                autoComplete="new-password"
                placeholder="Mínimo 8 caracteres"
              />
            </div>
            <button
              type="submit"
              disabled={pwGuardando}
              className="w-full py-2.5 rounded-xl bg-indigo-600 hover:bg-indigo-700 text-white text-sm font-semibold transition disabled:opacity-50"
            >
              {pwGuardando ? 'Guardando...' : 'Actualizar contraseña'}
            </button>
          </form>
        </div>

        <div className="mt-4 text-center">
          <Link href="/catalogo" className="text-sm text-gray-400 hover:text-indigo-600 transition-colors">
            Volver al catálogo
          </Link>
        </div>
      </div>
    </main>
  );
}

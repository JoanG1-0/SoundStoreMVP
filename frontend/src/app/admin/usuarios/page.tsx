'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { useAuth } from '@/context/AuthContext';
import { api } from '@/lib/api';
import { Pedido, EstadoPedido } from '@/types';

const formatearPrecio = (valor: number) =>
  new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP', maximumFractionDigits: 0 }).format(valor);

const formatearFecha = (iso: string) =>
  new Date(iso).toLocaleDateString('es-CO', { day: '2-digit', month: 'short', year: 'numeric' });

const etiquetaRol: Record<string, string> = {
  BUYER:  'Comprador',
  SELLER: 'Vendedor',
  ADMIN:  'Admin',
};

const colorRol: Record<string, string> = {
  BUYER:  'bg-blue-50 text-blue-700',
  SELLER: 'bg-indigo-50 text-indigo-700',
  ADMIN:  'bg-purple-50 text-purple-700',
};

const etiquetaEstado: Record<EstadoPedido, string> = {
  PENDING:      'Pendiente',
  CONFIRMED:    'Confirmado',
  PREPARING:    'En preparación',
  ON_THE_WAY:   'En camino',
  READY_PICKUP: 'Listo para recoger',
  DELIVERED:    'Entregado',
  CANCELLED:    'Cancelado',
};

interface UsuarioAdmin {
  id: string;
  fullName: string;
  email: string;
  phone: string;
  role: string;
  active: boolean;
  emailVerified: boolean;
  mustChangePassword: boolean;
}

interface FormUsuario {
  fullName: string;
  email: string;
  password: string;
  phone: string;
  role: 'SELLER' | 'ADMIN';
}

const formVacio: FormUsuario = { fullName: '', email: '', password: '', phone: '', role: 'SELLER' };

export default function AdminUsuariosPage() {
  const { isAuthenticated, getToken } = useAuth();
  const router = useRouter();

  const [usuarios, setUsuarios] = useState<UsuarioAdmin[]>([]);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [modalAbierto, setModalAbierto] = useState(false);
  const [form, setForm] = useState<FormUsuario>(formVacio);
  const [guardando, setGuardando] = useState(false);
  const [errorForm, setErrorForm] = useState<string | null>(null);

  const [expandido, setExpandido] = useState<string | null>(null);
  const [pedidosUsuario, setPedidosUsuario] = useState<Record<string, Pedido[]>>({});
  const [cargandoPedidos, setCargandoPedidos] = useState<string | null>(null);

  const cargarUsuarios = (token: string) => {
    api.get<UsuarioAdmin[]>('/admin/usuarios', token)
      .then(setUsuarios)
      .catch(() => setError('No se pudieron cargar los usuarios'))
      .finally(() => setCargando(false));
  };

  useEffect(() => {
    if (!isAuthenticated) { router.replace('/login'); return; }
    const token = getToken();
    if (token) cargarUsuarios(token);
  }, [isAuthenticated, getToken, router]);

  const abrirCrear = () => {
    setForm(formVacio);
    setErrorForm(null);
    setModalAbierto(true);
  };

  const cerrarModal = () => { setModalAbierto(false); };

  const handleCrear = async () => {
    const token = getToken();
    if (!token) return;
    setGuardando(true);
    setErrorForm(null);
    try {
      const nuevo = await api.post<UsuarioAdmin>('/admin/usuarios', {
        fullName: form.fullName.trim(),
        email: form.email.trim(),
        password: form.password,
        phone: form.phone.trim(),
        role: form.role,
      }, token);
      setUsuarios(prev => [nuevo, ...prev]);
      cerrarModal();
    } catch (e: unknown) {
      const err = e as { message?: string };
      setErrorForm(err?.message ?? 'Error al crear el usuario');
    } finally {
      setGuardando(false);
    }
  };

  const toggleEstado = async (u: UsuarioAdmin) => {
    const token = getToken();
    if (!token) return;
    try {
      const actualizado = await api.patch<UsuarioAdmin>(`/admin/usuarios/${u.id}/estado`, { active: !u.active }, token);
      setUsuarios(prev => prev.map(x => x.id === u.id ? actualizado : x));
    } catch {
      alert('No se pudo cambiar el estado del usuario');
    }
  };

  const togglePedidos = async (userId: string) => {
    if (expandido === userId) {
      setExpandido(null);
      return;
    }
    setExpandido(userId);
    if (pedidosUsuario[userId]) return;

    const token = getToken();
    if (!token) return;
    setCargandoPedidos(userId);
    try {
      const pedidos = await api.get<Pedido[]>(`/admin/usuarios/${userId}/pedidos`, token);
      setPedidosUsuario(prev => ({ ...prev, [userId]: pedidos }));
    } catch {
      setPedidosUsuario(prev => ({ ...prev, [userId]: [] }));
    } finally {
      setCargandoPedidos(null);
    }
  };

  if (cargando) return (
    <main className="min-h-screen bg-gray-50 flex items-center justify-center">
      <p className="text-gray-400 animate-pulse">Cargando usuarios...</p>
    </main>
  );

  if (error) return (
    <main className="min-h-screen bg-gray-50 flex items-center justify-center">
      <p className="text-red-500">{error}</p>
    </main>
  );

  return (
    <main className="min-h-screen bg-gray-50">
      <div className="max-w-6xl mx-auto px-4 sm:px-6 py-8">

        <div className="flex items-center justify-between mb-6">
          <h1 className="text-2xl font-bold text-gray-900">Gestión de usuarios</h1>
          <button
            onClick={abrirCrear}
            className="px-4 py-2 bg-indigo-600 text-white text-sm font-medium rounded-xl hover:bg-indigo-700 transition-colors"
          >
            + Nuevo usuario
          </button>
        </div>

        {usuarios.length === 0 ? (
          <div className="bg-white rounded-2xl border border-gray-100 p-12 text-center text-gray-400">
            No hay usuarios registrados.
          </div>
        ) : (
          <div className="bg-white rounded-2xl border border-gray-100 shadow-sm overflow-hidden">
            <div className="overflow-x-auto">
              <table className="w-full text-sm">
                <thead className="bg-gray-50 border-b border-gray-100">
                  <tr>
                    <th className="text-left px-4 py-3 text-xs font-semibold text-gray-400 uppercase tracking-wide">Usuario</th>
                    <th className="text-left px-4 py-3 text-xs font-semibold text-gray-400 uppercase tracking-wide">Teléfono</th>
                    <th className="text-center px-4 py-3 text-xs font-semibold text-gray-400 uppercase tracking-wide">Rol</th>
                    <th className="text-center px-4 py-3 text-xs font-semibold text-gray-400 uppercase tracking-wide">Estado</th>
                    <th className="text-center px-4 py-3 text-xs font-semibold text-gray-400 uppercase tracking-wide">Acciones</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-50">
                  {usuarios.map(u => {
                    const abierto = expandido === u.id;
                    const pedidos = pedidosUsuario[u.id] ?? [];

                    return (
                      <>
                        <tr key={u.id} className={`hover:bg-gray-50 transition-colors ${!u.active ? 'opacity-50' : ''}`}>
                          <td className="px-4 py-3">
                            <p className="font-medium text-gray-900">{u.fullName}</p>
                            <p className="text-xs text-gray-400">{u.email}</p>
                            {u.mustChangePassword && (
                              <span className="inline-flex mt-0.5 px-1.5 py-0.5 bg-amber-50 text-amber-600 text-xs rounded">
                                Cambio de contraseña pendiente
                              </span>
                            )}
                          </td>
                          <td className="px-4 py-3 text-gray-500">{u.phone}</td>
                          <td className="px-4 py-3 text-center">
                            <span className={`inline-flex px-2 py-0.5 rounded-full text-xs font-medium ${colorRol[u.role] ?? 'bg-gray-100 text-gray-600'}`}>
                              {etiquetaRol[u.role] ?? u.role}
                            </span>
                          </td>
                          <td className="px-4 py-3 text-center">
                            <span className={`inline-flex px-2 py-0.5 rounded-full text-xs font-medium ${
                              u.active ? 'bg-green-50 text-green-700' : 'bg-gray-100 text-gray-500'
                            }`}>
                              {u.active ? 'Activo' : 'Inactivo'}
                            </span>
                          </td>
                          <td className="px-4 py-3 text-center">
                            <div className="flex items-center justify-center gap-2">
                              <button
                                onClick={() => toggleEstado(u)}
                                className={`text-xs px-3 py-1.5 rounded-lg transition-colors ${
                                  u.active
                                    ? 'border border-red-200 text-red-600 hover:bg-red-50'
                                    : 'border border-green-200 text-green-600 hover:bg-green-50'
                                }`}
                              >
                                {u.active ? 'Desactivar' : 'Activar'}
                              </button>
                              <button
                                onClick={() => togglePedidos(u.id)}
                                className="text-xs px-3 py-1.5 border border-gray-200 rounded-lg hover:bg-gray-50 transition-colors text-gray-600 flex items-center gap-1"
                              >
                                Pedidos
                                <svg className={`w-3.5 h-3.5 transition-transform ${abierto ? 'rotate-180' : ''}`}
                                  fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" />
                                </svg>
                              </button>
                            </div>
                          </td>
                        </tr>

                        {abierto && (
                          <tr key={`${u.id}-pedidos`}>
                            <td colSpan={5} className="bg-gray-50/70 px-6 py-4 border-t border-gray-100">
                              {cargandoPedidos === u.id ? (
                                <p className="text-xs text-gray-400 animate-pulse">Cargando pedidos...</p>
                              ) : pedidos.length === 0 ? (
                                <p className="text-xs text-gray-400">Este usuario no tiene pedidos.</p>
                              ) : (
                                <div className="space-y-2">
                                  {pedidos.map(p => (
                                    <div key={p.id} className="flex items-center justify-between text-xs text-gray-600 bg-white rounded-lg px-3 py-2 border border-gray-100">
                                      <span className="font-bold text-indigo-600">{p.orderNumber}</span>
                                      <span className="text-gray-400">{formatearFecha(p.createdAt)}</span>
                                      <span>{etiquetaEstado[p.status]}</span>
                                      <span className="font-medium text-gray-900">{formatearPrecio(p.total)}</span>
                                    </div>
                                  ))}
                                </div>
                              )}
                            </td>
                          </tr>
                        )}
                      </>
                    );
                  })}
                </tbody>
              </table>
            </div>
          </div>
        )}
      </div>

      {modalAbierto && (
        <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50 px-4">
          <div className="bg-white rounded-2xl shadow-xl w-full max-w-md p-6">
            <h2 className="text-lg font-bold text-gray-900 mb-5">Nuevo usuario</h2>

            <div className="space-y-4">
              <div>
                <label className="block text-xs font-medium text-gray-600 mb-1">Nombre completo *</label>
                <input
                  className="w-full border border-gray-200 rounded-xl px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-300"
                  value={form.fullName}
                  onChange={e => setForm(f => ({ ...f, fullName: e.target.value }))}
                  placeholder="Nombre y apellido"
                  maxLength={120}
                />
              </div>

              <div>
                <label className="block text-xs font-medium text-gray-600 mb-1">Correo electrónico *</label>
                <input
                  type="email"
                  className="w-full border border-gray-200 rounded-xl px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-300"
                  value={form.email}
                  onChange={e => setForm(f => ({ ...f, email: e.target.value }))}
                  placeholder="correo@ejemplo.com"
                />
              </div>

              <div>
                <label className="block text-xs font-medium text-gray-600 mb-1">Contraseña temporal *</label>
                <input
                  type="password"
                  className="w-full border border-gray-200 rounded-xl px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-300"
                  value={form.password}
                  onChange={e => setForm(f => ({ ...f, password: e.target.value }))}
                  placeholder="Mínimo 8 caracteres"
                />
              </div>

              <div>
                <label className="block text-xs font-medium text-gray-600 mb-1">Teléfono *</label>
                <input
                  className="w-full border border-gray-200 rounded-xl px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-300"
                  value={form.phone}
                  onChange={e => setForm(f => ({ ...f, phone: e.target.value }))}
                  placeholder="Número de contacto"
                  maxLength={20}
                />
              </div>

              <div>
                <label className="block text-xs font-medium text-gray-600 mb-1">Rol *</label>
                <select
                  className="w-full border border-gray-200 rounded-xl px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-300 bg-white"
                  value={form.role}
                  onChange={e => setForm(f => ({ ...f, role: e.target.value as 'SELLER' | 'ADMIN' }))}
                >
                  <option value="SELLER">Vendedor</option>
                  <option value="ADMIN">Admin</option>
                </select>
              </div>

              {errorForm && (
                <p className="text-sm text-red-500">{errorForm}</p>
              )}
            </div>

            <div className="flex gap-3 mt-6">
              <button
                onClick={cerrarModal}
                className="flex-1 py-2.5 border border-gray-200 rounded-xl text-sm text-gray-600 hover:bg-gray-50 transition-colors"
              >
                Cancelar
              </button>
              <button
                onClick={handleCrear}
                disabled={guardando}
                className="flex-1 py-2.5 bg-indigo-600 text-white text-sm font-medium rounded-xl hover:bg-indigo-700 transition-colors disabled:opacity-50"
              >
                {guardando ? 'Creando...' : 'Crear usuario'}
              </button>
            </div>
          </div>
        </div>
      )}
    </main>
  );
}

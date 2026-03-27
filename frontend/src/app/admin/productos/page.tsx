'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import Image from 'next/image';
import { useAuth } from '@/context/AuthContext';
import { api } from '@/lib/api';
import { Producto } from '@/types';

const formatearPrecio = (valor: number) =>
  new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP', maximumFractionDigits: 0 }).format(valor);

interface FormProducto {
  name: string;
  description: string;
  price: string;
  genre: string;
  stock: string;
  imageUrl: string;
}

const formVacio: FormProducto = { name: '', description: '', price: '', genre: '', stock: '0', imageUrl: '' };

export default function AdminProductosPage() {
  const { isAuthenticated, getToken } = useAuth();
  const router = useRouter();

  const [productos, setProductos] = useState<Producto[]>([]);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [modalAbierto, setModalAbierto] = useState(false);
  const [editando, setEditando] = useState<Producto | null>(null);
  const [form, setForm] = useState<FormProducto>(formVacio);
  const [guardando, setGuardando] = useState(false);
  const [errorForm, setErrorForm] = useState<string | null>(null);

  const cargarProductos = (token: string) => {
    api.get<Producto[]>('/products/gestion', token)
      .then(setProductos)
      .catch(() => setError('No se pudieron cargar los productos'))
      .finally(() => setCargando(false));
  };

  useEffect(() => {
    if (!isAuthenticated) { router.replace('/login'); return; }
    const token = getToken();
    if (token) cargarProductos(token);
  }, [isAuthenticated, getToken, router]);

  const abrirCrear = () => {
    setEditando(null);
    setForm(formVacio);
    setErrorForm(null);
    setModalAbierto(true);
  };

  const abrirEditar = (p: Producto) => {
    setEditando(p);
    setForm({
      name: p.name,
      description: p.description,
      price: String(p.price),
      genre: p.genre,
      stock: String(p.stock),
      imageUrl: p.imageUrl ?? '',
    });
    setErrorForm(null);
    setModalAbierto(true);
  };

  const cerrarModal = () => { setModalAbierto(false); setEditando(null); };

  const handleGuardar = async () => {
    const token = getToken();
    if (!token) return;

    setGuardando(true);
    setErrorForm(null);

    const payload = {
      name: form.name.trim(),
      description: form.description.trim(),
      price: parseFloat(form.price),
      genre: form.genre.trim(),
      stock: parseInt(form.stock, 10),
      imageUrl: form.imageUrl.trim() || null,
    };

    try {
      if (editando) {
        const actualizado = await api.put<Producto>(`/products/${editando.id}`, payload, token);
        setProductos(prev => prev.map(p => p.id === editando.id ? actualizado : p));
      } else {
        const nuevo = await api.post<Producto>('/products', payload, token);
        setProductos(prev => [nuevo, ...prev]);
      }
      cerrarModal();
    } catch (e: unknown) {
      const err = e as { message?: string };
      setErrorForm(err?.message ?? 'Error al guardar el producto');
    } finally {
      setGuardando(false);
    }
  };

  const toggleEstado = async (p: Producto) => {
    const token = getToken();
    if (!token) return;
    try {
      const actualizado = await api.patch<Producto>(`/products/${p.id}/estado`, { active: !p.active }, token);
      setProductos(prev => prev.map(x => x.id === p.id ? actualizado : x));
    } catch {
      alert('No se pudo cambiar el estado del producto');
    }
  };

  if (cargando) return (
    <main className="min-h-screen bg-gray-50 flex items-center justify-center">
      <p className="text-gray-400 animate-pulse">Cargando productos...</p>
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

        {/* Encabezado */}
        <div className="flex items-center justify-between mb-6">
          <h1 className="text-2xl font-bold text-gray-900">Gestión de productos</h1>
          <button
            onClick={abrirCrear}
            className="px-4 py-2 bg-indigo-600 text-white text-sm font-medium rounded-xl hover:bg-indigo-700 transition-colors"
          >
            + Nuevo producto
          </button>
        </div>

        {/* Tabla */}
        {productos.length === 0 ? (
          <div className="bg-white rounded-2xl border border-gray-100 p-12 text-center text-gray-400">
            No hay productos registrados aún.
          </div>
        ) : (
          <div className="bg-white rounded-2xl border border-gray-100 shadow-sm overflow-hidden">
            <div className="overflow-x-auto">
              <table className="w-full text-sm">
                <thead className="bg-gray-50 border-b border-gray-100">
                  <tr>
                    <th className="text-left px-4 py-3 text-xs font-semibold text-gray-400 uppercase tracking-wide">Producto</th>
                    <th className="text-left px-4 py-3 text-xs font-semibold text-gray-400 uppercase tracking-wide">Género</th>
                    <th className="text-right px-4 py-3 text-xs font-semibold text-gray-400 uppercase tracking-wide">Precio</th>
                    <th className="text-right px-4 py-3 text-xs font-semibold text-gray-400 uppercase tracking-wide">Stock</th>
                    <th className="text-center px-4 py-3 text-xs font-semibold text-gray-400 uppercase tracking-wide">Estado</th>
                    <th className="text-center px-4 py-3 text-xs font-semibold text-gray-400 uppercase tracking-wide">Acciones</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-50">
                  {productos.map(p => (
                    <tr key={p.id} className={`hover:bg-gray-50 transition-colors ${!p.active ? 'opacity-50' : ''}`}>
                      <td className="px-4 py-3">
                        <div className="flex items-center gap-3">
                          {p.imageUrl ? (
                            <Image src={p.imageUrl} alt={p.name} width={40} height={40}
                              className="w-10 h-10 rounded-lg object-cover bg-gray-100" />
                          ) : (
                            <div className="w-10 h-10 rounded-lg bg-gray-100 flex items-center justify-center text-gray-300">
                              <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2}
                                  d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
                              </svg>
                            </div>
                          )}
                          <span className="font-medium text-gray-900 line-clamp-1">{p.name}</span>
                        </div>
                      </td>
                      <td className="px-4 py-3 text-gray-500">{p.genre}</td>
                      <td className="px-4 py-3 text-right font-medium text-gray-900">{formatearPrecio(p.price)}</td>
                      <td className="px-4 py-3 text-right">
                        <span className={`font-semibold ${p.stock < 5 ? 'text-red-600' : 'text-gray-900'}`}>
                          {p.stock}
                        </span>
                      </td>
                      <td className="px-4 py-3 text-center">
                        <span className={`inline-flex px-2 py-0.5 rounded-full text-xs font-medium ${
                          p.active ? 'bg-green-50 text-green-700' : 'bg-gray-100 text-gray-500'
                        }`}>
                          {p.active ? 'Activo' : 'Inactivo'}
                        </span>
                      </td>
                      <td className="px-4 py-3 text-center">
                        <div className="flex items-center justify-center gap-2">
                          <button
                            onClick={() => abrirEditar(p)}
                            className="text-xs px-3 py-1.5 border border-gray-200 rounded-lg hover:bg-gray-50 transition-colors text-gray-600"
                          >
                            Editar
                          </button>
                          <button
                            onClick={() => toggleEstado(p)}
                            className={`text-xs px-3 py-1.5 rounded-lg transition-colors ${
                              p.active
                                ? 'border border-red-200 text-red-600 hover:bg-red-50'
                                : 'border border-green-200 text-green-600 hover:bg-green-50'
                            }`}
                          >
                            {p.active ? 'Desactivar' : 'Activar'}
                          </button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        )}
      </div>

      {/* Modal crear/editar */}
      {modalAbierto && (
        <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50 px-4">
          <div className="bg-white rounded-2xl shadow-xl w-full max-w-lg max-h-[90vh] overflow-y-auto p-6">
            <h2 className="text-lg font-bold text-gray-900 mb-5">
              {editando ? 'Editar producto' : 'Nuevo producto'}
            </h2>

            <div className="space-y-4">
              <div>
                <label className="block text-xs font-medium text-gray-600 mb-1">Nombre *</label>
                <input
                  className="w-full border border-gray-200 rounded-xl px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-300"
                  value={form.name}
                  onChange={e => setForm(f => ({ ...f, name: e.target.value }))}
                  placeholder="Nombre del producto"
                  maxLength={150}
                />
              </div>

              <div>
                <label className="block text-xs font-medium text-gray-600 mb-1">Descripción *</label>
                <textarea
                  className="w-full border border-gray-200 rounded-xl px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-300 resize-none"
                  rows={3}
                  value={form.description}
                  onChange={e => setForm(f => ({ ...f, description: e.target.value }))}
                  placeholder="Descripción del contenido musical"
                />
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-medium text-gray-600 mb-1">Precio (COP) *</label>
                  <input
                    type="number"
                    min="0.01"
                    step="0.01"
                    className="w-full border border-gray-200 rounded-xl px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-300"
                    value={form.price}
                    onChange={e => setForm(f => ({ ...f, price: e.target.value }))}
                    placeholder="0"
                  />
                </div>
                <div>
                  <label className="block text-xs font-medium text-gray-600 mb-1">Stock *</label>
                  <input
                    type="number"
                    min="0"
                    className="w-full border border-gray-200 rounded-xl px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-300"
                    value={form.stock}
                    onChange={e => setForm(f => ({ ...f, stock: e.target.value }))}
                    placeholder="0"
                  />
                </div>
              </div>

              <div>
                <label className="block text-xs font-medium text-gray-600 mb-1">Género musical *</label>
                <input
                  className="w-full border border-gray-200 rounded-xl px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-300"
                  value={form.genre}
                  onChange={e => setForm(f => ({ ...f, genre: e.target.value }))}
                  placeholder="Ej: Salsa, Vallenato, Rock..."
                  maxLength={80}
                />
              </div>

              <div>
                <label className="block text-xs font-medium text-gray-600 mb-1">URL de imagen</label>
                <input
                  className="w-full border border-gray-200 rounded-xl px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-300"
                  value={form.imageUrl}
                  onChange={e => setForm(f => ({ ...f, imageUrl: e.target.value }))}
                  placeholder="https://..."
                />
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
                onClick={handleGuardar}
                disabled={guardando}
                className="flex-1 py-2.5 bg-indigo-600 text-white text-sm font-medium rounded-xl hover:bg-indigo-700 transition-colors disabled:opacity-50"
              >
                {guardando ? 'Guardando...' : editando ? 'Guardar cambios' : 'Crear producto'}
              </button>
            </div>
          </div>
        </div>
      )}
    </main>
  );
}

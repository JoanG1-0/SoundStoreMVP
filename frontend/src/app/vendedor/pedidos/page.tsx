'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { useAuth } from '@/context/AuthContext';
import { api } from '@/lib/api';
import { Pedido, EstadoPedido, TipoEntrega } from '@/types';

const formatearPrecio = (valor: number) =>
  new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP', maximumFractionDigits: 0 }).format(valor);

const formatearFecha = (iso: string) =>
  new Date(iso).toLocaleDateString('es-CO', { day: '2-digit', month: 'short', year: 'numeric', hour: '2-digit', minute: '2-digit' });

const etiquetaEstado: Record<EstadoPedido, string> = {
  PENDING:      'Pendiente',
  CONFIRMED:    'Confirmado',
  PREPARING:    'En preparación',
  ON_THE_WAY:   'En camino',
  READY_PICKUP: 'Listo para recoger',
  DELIVERED:    'Entregado',
  CANCELLED:    'Cancelado',
};

const colorEstado: Record<EstadoPedido, string> = {
  PENDING:      'bg-amber-50 text-amber-700 border-amber-200',
  CONFIRMED:    'bg-blue-50 text-blue-700 border-blue-200',
  PREPARING:    'bg-indigo-50 text-indigo-700 border-indigo-200',
  ON_THE_WAY:   'bg-purple-50 text-purple-700 border-purple-200',
  READY_PICKUP: 'bg-purple-50 text-purple-700 border-purple-200',
  DELIVERED:    'bg-green-50 text-green-700 border-green-200',
  CANCELLED:    'bg-red-50 text-red-700 border-red-200',
};

function siguientesEstados(estado: EstadoPedido, tipoEntrega: TipoEntrega): EstadoPedido[] {
  switch (estado) {
    case 'PENDING':   return ['CONFIRMED', 'CANCELLED'];
    case 'CONFIRMED': return ['PREPARING', 'CANCELLED'];
    case 'PREPARING': return tipoEntrega === 'DELIVERY' ? ['ON_THE_WAY'] : ['READY_PICKUP'];
    case 'ON_THE_WAY':   return ['DELIVERED'];
    case 'READY_PICKUP': return ['DELIVERED'];
    default: return [];
  }
}

interface PedidoConExtra extends Pedido {
  userName?: string;
  userEmail?: string;
}

export default function VendedorPedidosPage() {
  const { isAuthenticated, getToken } = useAuth();
  const router = useRouter();

  const [pedidos, setPedidos] = useState<PedidoConExtra[]>([]);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [expandido, setExpandido] = useState<string | null>(null);
  const [cambiando, setCambiando] = useState<string | null>(null);

  useEffect(() => {
    if (!isAuthenticated) { router.replace('/login'); return; }
    const token = getToken();
    if (!token) return;

    api.get<PedidoConExtra[]>('/orders', token)
      .then(setPedidos)
      .catch(() => setError('No se pudieron cargar los pedidos'))
      .finally(() => setCargando(false));
  }, [isAuthenticated, getToken, router]);

  const cambiarEstado = async (pedidoId: string, nuevoEstado: EstadoPedido) => {
    const token = getToken();
    if (!token) return;
    setCambiando(pedidoId);
    try {
      const actualizado = await api.patch<PedidoConExtra>(`/orders/${pedidoId}/estado`, { newStatus: nuevoEstado }, token);
      setPedidos(prev => prev.map(p => p.id === pedidoId ? { ...actualizado, userName: p.userName, userEmail: p.userEmail } : p));
    } catch (e: unknown) {
      const err = e as { message?: string };
      alert(err?.message ?? 'No se pudo cambiar el estado');
    } finally {
      setCambiando(null);
    }
  };

  if (cargando) return (
    <main className="min-h-screen bg-gray-50 flex items-center justify-center">
      <p className="text-gray-400 animate-pulse">Cargando pedidos...</p>
    </main>
  );

  if (error) return (
    <main className="min-h-screen bg-gray-50 flex items-center justify-center">
      <p className="text-red-500">{error}</p>
    </main>
  );

  return (
    <main className="min-h-screen bg-gray-50">
      <div className="max-w-5xl mx-auto px-4 sm:px-6 py-8">
        <div className="flex items-center justify-between mb-6">
          <h1 className="text-2xl font-bold text-gray-900">Pedidos activos</h1>
          <span className="text-sm text-gray-400">{pedidos.length} pedido{pedidos.length !== 1 ? 's' : ''}</span>
        </div>

        {pedidos.length === 0 ? (
          <div className="bg-white rounded-2xl border border-gray-100 p-12 text-center text-gray-400">
            No hay pedidos activos en este momento.
          </div>
        ) : (
          <div className="space-y-3">
            {pedidos.map(pedido => {
              const siguientes = siguientesEstados(pedido.status, pedido.deliveryType);
              const abierto = expandido === pedido.id;

              return (
                <div key={pedido.id} className="bg-white rounded-2xl border border-gray-100 shadow-sm overflow-hidden">
                  <div className="p-4 flex flex-col sm:flex-row sm:items-center gap-3">
                    <div className="flex-1 min-w-0">
                      <div className="flex items-center gap-2 flex-wrap">
                        <span className="font-bold text-indigo-600 text-sm">{pedido.orderNumber}</span>
                        <span className={`inline-flex px-2 py-0.5 rounded-full text-xs font-medium border ${colorEstado[pedido.status]}`}>
                          {etiquetaEstado[pedido.status]}
                        </span>
                        <span className="text-xs text-gray-400">
                          {pedido.deliveryType === 'DELIVERY' ? 'Domicilio' : 'Recogida'}
                        </span>
                      </div>
                      <p className="text-xs text-gray-400 mt-0.5">{formatearFecha(pedido.createdAt)}</p>
                      {pedido.userName && (
                        <p className="text-xs text-gray-500 mt-0.5">
                          <span className="font-medium">{pedido.userName}</span>
                          {pedido.userEmail && <span className="text-gray-400"> · {pedido.userEmail}</span>}
                        </p>
                      )}
                    </div>

                    <div className="flex items-center gap-3">
                      <span className="font-bold text-gray-900">{formatearPrecio(pedido.total)}</span>

                      {siguientes.length > 0 && (
                        <div className="flex gap-1.5 flex-wrap">
                          {siguientes.map(sig => (
                            <button
                              key={sig}
                              onClick={() => cambiarEstado(pedido.id, sig)}
                              disabled={cambiando === pedido.id}
                              className={`text-xs px-3 py-1.5 rounded-lg font-medium transition-colors disabled:opacity-50 ${
                                sig === 'CANCELLED'
                                  ? 'border border-red-200 text-red-600 hover:bg-red-50'
                                  : 'border border-indigo-200 text-indigo-600 hover:bg-indigo-50'
                              }`}
                            >
                              {cambiando === pedido.id ? '...' : etiquetaEstado[sig]}
                            </button>
                          ))}
                        </div>
                      )}

                      <button
                        onClick={() => setExpandido(abierto ? null : pedido.id)}
                        className="text-gray-400 hover:text-gray-600 transition-colors"
                        aria-label="Ver detalle"
                      >
                        <svg className={`w-5 h-5 transition-transform ${abierto ? 'rotate-180' : ''}`}
                          fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" />
                        </svg>
                      </button>
                    </div>
                  </div>

                  {abierto && (
                    <div className="border-t border-gray-50 px-4 py-3 bg-gray-50/50">
                      {pedido.deliveryAddress && (
                        <p className="text-xs text-gray-500 mb-2">
                          <span className="font-medium">Dirección:</span> {pedido.deliveryAddress}
                        </p>
                      )}
                      <div className="space-y-1">
                        {pedido.items.map(item => (
                          <div key={item.id} className="flex justify-between text-xs text-gray-600">
                            <span>{item.productName} <span className="text-gray-400">×{item.quantity}</span></span>
                            <span className="font-medium">{formatearPrecio(item.subtotal)}</span>
                          </div>
                        ))}
                      </div>
                    </div>
                  )}
                </div>
              );
            })}
          </div>
        )}
      </div>
    </main>
  );
}

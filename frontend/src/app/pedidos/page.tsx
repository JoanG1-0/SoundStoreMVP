'use client';

import { useEffect, useState } from 'react';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { useAuth } from '@/context/AuthContext';
import { api } from '@/lib/api';
import { Pedido, EstadoPedido } from '@/types';

const formatearPrecio = (precio: number) =>
  new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP', maximumFractionDigits: 0 }).format(precio);

const formatearFecha = (iso: string) =>
  new Date(iso).toLocaleDateString('es-CO', { day: '2-digit', month: 'short', year: 'numeric' });

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

export default function PedidosPage() {
  const { isAuthenticated, getToken } = useAuth();
  const router = useRouter();
  const [pedidos, setPedidos] = useState<Pedido[]>([]);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!isAuthenticated) {
      router.replace('/login');
      return;
    }
    const token = getToken();
    if (!token) return;

    api.get<Pedido[]>('/orders/mis-pedidos', token)
      .then(setPedidos)
      .catch(() => setError('No se pudo cargar el historial de pedidos'))
      .finally(() => setCargando(false));
  }, [isAuthenticated, getToken, router]);

  if (cargando) {
    return (
      <main className="min-h-screen bg-gray-50 flex items-center justify-center">
        <p className="text-gray-400 animate-pulse">Cargando pedidos...</p>
      </main>
    );
  }

  if (error) {
    return (
      <main className="min-h-screen bg-gray-50 flex items-center justify-center">
        <p className="text-red-500">{error}</p>
      </main>
    );
  }

  return (
    <main className="min-h-screen bg-gray-50">
      <div className="max-w-3xl mx-auto px-4 sm:px-6 py-8">
        <h1 className="text-2xl font-bold text-gray-900 mb-6">Mis pedidos</h1>

        {pedidos.length === 0 ? (
          <div className="text-center py-16">
            <p className="text-gray-500 mb-4">Aún no has realizado ningún pedido.</p>
            <Link
              href="/catalogo"
              className="inline-block px-6 py-3 bg-indigo-600 text-white rounded-xl font-medium hover:bg-indigo-700 transition-colors text-sm"
            >
              Ver catálogo
            </Link>
          </div>
        ) : (
          <div className="space-y-3">
            {pedidos.map((pedido) => (
              <Link
                key={pedido.id}
                href={`/pedidos/${pedido.id}`}
                className="block bg-white rounded-2xl border border-gray-100 shadow-sm p-5 hover:border-indigo-200 hover:shadow-md transition-all"
              >
                <div className="flex items-start justify-between gap-3">
                  <div>
                    <p className="font-bold text-indigo-600 text-sm">{pedido.orderNumber}</p>
                    <p className="text-xs text-gray-400 mt-0.5">{formatearFecha(pedido.createdAt)}</p>
                    <p className="text-xs text-gray-500 mt-1">
                      {pedido.deliveryType === 'DELIVERY' ? 'Envío a domicilio' : 'Recoge en tienda'}
                      {' · '}
                      {pedido.items.length} {pedido.items.length === 1 ? 'producto' : 'productos'}
                    </p>
                  </div>
                  <div className="flex flex-col items-end gap-2">
                    <span className={`inline-flex px-2.5 py-1 rounded-full text-xs font-medium border ${colorEstado[pedido.status]}`}>
                      {etiquetaEstado[pedido.status]}
                    </span>
                    <span className="font-bold text-gray-900 text-sm">{formatearPrecio(pedido.total)}</span>
                  </div>
                </div>
              </Link>
            ))}
          </div>
        )}
      </div>
    </main>
  );
}

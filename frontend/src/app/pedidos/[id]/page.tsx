'use client';

import { useEffect, useState } from 'react';
import Link from 'next/link';
import { useParams, useRouter } from 'next/navigation';
import { useAuth } from '@/context/AuthContext';
import { api } from '@/lib/api';
import { Pedido, EstadoPedido } from '@/types';

const formatearPrecio = (precio: number) =>
  new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP', maximumFractionDigits: 0 }).format(precio);

const formatearFecha = (iso: string) =>
  new Date(iso).toLocaleDateString('es-CO', { day: '2-digit', month: 'long', year: 'numeric', hour: '2-digit', minute: '2-digit' });

const etiquetaEstado: Record<EstadoPedido, string> = {
  PENDING:      'Pendiente de confirmación',
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

const pasosEstado: EstadoPedido[] = [
  'PENDING', 'CONFIRMED', 'PREPARING', 'ON_THE_WAY', 'DELIVERED',
];

const pasoPickup: EstadoPedido[] = [
  'PENDING', 'CONFIRMED', 'PREPARING', 'READY_PICKUP', 'DELIVERED',
];

export default function DetallePedidoPage() {
  const { id } = useParams<{ id: string }>();
  const { isAuthenticated, getToken } = useAuth();
  const router = useRouter();
  const [pedido, setPedido] = useState<Pedido | null>(null);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!isAuthenticated) {
      router.replace('/login');
      return;
    }
    const token = getToken();
    if (!token) return;

    api.get<Pedido>(`/orders/${id}`, token)
      .then(setPedido)
      .catch(() => setError('No se encontró el pedido'))
      .finally(() => setCargando(false));
  }, [id, isAuthenticated, getToken, router]);

  if (cargando) {
    return (
      <main className="min-h-screen bg-gray-50 flex items-center justify-center">
        <p className="text-gray-400 animate-pulse">Cargando pedido...</p>
      </main>
    );
  }

  if (error || !pedido) {
    return (
      <main className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <p className="text-red-500 mb-4">{error ?? 'Pedido no encontrado'}</p>
          <Link href="/pedidos" className="text-indigo-600 text-sm hover:underline">← Volver a mis pedidos</Link>
        </div>
      </main>
    );
  }

  const pasos = pedido.deliveryType === 'PICKUP' ? pasoPickup : pasosEstado;
  const indexActual = pasos.indexOf(pedido.status);

  return (
    <main className="min-h-screen bg-gray-50">
      <div className="max-w-2xl mx-auto px-4 sm:px-6 py-8">

        {/* Cabecera */}
        <div className="flex items-center gap-3 mb-6">
          <Link href="/pedidos" className="text-gray-400 hover:text-gray-600 transition-colors">
            <svg xmlns="http://www.w3.org/2000/svg" className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
              <path strokeLinecap="round" strokeLinejoin="round" d="M15 19l-7-7 7-7" />
            </svg>
          </Link>
          <div>
            <h1 className="text-xl font-bold text-gray-900">{pedido.orderNumber}</h1>
            <p className="text-xs text-gray-400">{formatearFecha(pedido.createdAt)}</p>
          </div>
          <span className={`ml-auto inline-flex px-3 py-1 rounded-full text-xs font-medium border ${colorEstado[pedido.status]}`}>
            {etiquetaEstado[pedido.status]}
          </span>
        </div>

        {/* Progreso — solo si no está cancelado */}
        {pedido.status !== 'CANCELLED' && (
          <div className="bg-white rounded-2xl border border-gray-100 shadow-sm p-5 mb-4">
            <p className="text-xs font-medium text-gray-400 uppercase tracking-wide mb-4">Seguimiento</p>
            <div className="flex items-center gap-1">
              {pasos.map((paso, i) => {
                const completado = i <= indexActual;
                const esUltimo = i === pasos.length - 1;
                return (
                  <div key={paso} className="flex items-center flex-1 last:flex-none">
                    <div className={`w-3 h-3 rounded-full flex-shrink-0 ${completado ? 'bg-indigo-600' : 'bg-gray-200'}`} />
                    {!esUltimo && (
                      <div className={`flex-1 h-0.5 mx-1 ${i < indexActual ? 'bg-indigo-600' : 'bg-gray-200'}`} />
                    )}
                  </div>
                );
              })}
            </div>
            <div className="flex justify-between mt-2">
              {pasos.map((paso) => (
                <p key={paso} className="text-xs text-gray-400 text-center" style={{ width: `${100 / pasos.length}%` }}>
                  {etiquetaEstado[paso].split(' ')[0]}
                </p>
              ))}
            </div>
          </div>
        )}

        {/* Entrega */}
        <div className="bg-white rounded-2xl border border-gray-100 shadow-sm p-5 mb-4">
          <p className="text-xs font-medium text-gray-400 uppercase tracking-wide mb-2">Entrega</p>
          <p className="text-sm text-gray-700">
            {pedido.deliveryType === 'DELIVERY'
              ? `Envío a domicilio — ${pedido.deliveryAddress}`
              : 'Recoge en tienda'}
          </p>
        </div>

        {/* Productos */}
        <div className="bg-white rounded-2xl border border-gray-100 shadow-sm p-5 mb-4">
          <p className="text-xs font-medium text-gray-400 uppercase tracking-wide mb-3">Productos</p>
          <div className="space-y-3">
            {pedido.items.map((item) => (
              <div key={item.id} className="flex justify-between items-center text-sm">
                <div>
                  <p className="font-medium text-gray-900">{item.productName}</p>
                  <p className="text-xs text-gray-400">{formatearPrecio(item.unitPrice)} × {item.quantity}</p>
                </div>
                <span className="font-semibold text-gray-900">{formatearPrecio(item.subtotal)}</span>
              </div>
            ))}
          </div>
          <div className="border-t border-gray-100 mt-4 pt-4 flex justify-between font-bold text-gray-900">
            <span>Total</span>
            <span>{formatearPrecio(pedido.total)}</span>
          </div>
        </div>

      </div>
    </main>
  );
}

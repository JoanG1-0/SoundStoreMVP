'use client';

import { useEffect, useState } from 'react';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { Pedido } from '@/types';

const formatearPrecio = (precio: number) =>
  new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP', maximumFractionDigits: 0 }).format(precio);

const etiquetaEstado: Record<string, string> = {
  PENDING: 'Pendiente de confirmación',
  CONFIRMED: 'Confirmado',
  PREPARING: 'En preparación',
  ON_THE_WAY: 'En camino',
  READY_PICKUP: 'Listo para recoger',
  DELIVERED: 'Entregado',
  CANCELLED: 'Cancelado',
};

export default function CheckoutPage() {
  const router = useRouter();
  const [pedido, setPedido] = useState<Pedido | null>(null);

  useEffect(() => {
    const guardado = sessionStorage.getItem('pedido_confirmacion');
    if (!guardado) {
      router.replace('/catalogo');
      return;
    }
    try {
      setPedido(JSON.parse(guardado));
      sessionStorage.removeItem('pedido_confirmacion');
    } catch {
      router.replace('/catalogo');
    }
  }, [router]);

  if (!pedido) return null;

  return (
    <main className="min-h-screen bg-gray-50 flex items-center justify-center px-4 py-12">
      <div className="w-full max-w-lg">

        {/* Encabezado éxito */}
        <div className="text-center mb-8">
          <div className="w-16 h-16 bg-green-100 rounded-full flex items-center justify-center mx-auto mb-4">
            <svg xmlns="http://www.w3.org/2000/svg" className="w-8 h-8 text-green-600" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
              <path strokeLinecap="round" strokeLinejoin="round" d="M5 13l4 4L19 7" />
            </svg>
          </div>
          <h1 className="text-2xl font-bold text-gray-900">¡Pedido realizado!</h1>
          <p className="text-gray-500 mt-1 text-sm">Recibirás un correo con la confirmación</p>
        </div>

        {/* Tarjeta pedido */}
        <div className="bg-white rounded-2xl border border-gray-100 shadow-sm p-6 space-y-5">

          {/* Número y estado */}
          <div className="flex items-center justify-between">
            <div>
              <p className="text-xs text-gray-400 uppercase tracking-wide">Número de orden</p>
              <p className="text-xl font-bold text-indigo-600 mt-0.5">{pedido.orderNumber}</p>
            </div>
            <span className="inline-flex items-center px-3 py-1 rounded-full text-xs font-medium bg-amber-50 text-amber-700 border border-amber-200">
              {etiquetaEstado[pedido.status] ?? pedido.status}
            </span>
          </div>

          <hr className="border-gray-100" />

          {/* Modalidad */}
          <div className="flex items-center gap-2 text-sm text-gray-600">
            <svg xmlns="http://www.w3.org/2000/svg" className="w-4 h-4 text-gray-400 flex-shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
              {pedido.deliveryType === 'DELIVERY'
                ? <path strokeLinecap="round" strokeLinejoin="round" d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6" />
                : <path strokeLinecap="round" strokeLinejoin="round" d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4" />
              }
            </svg>
            <span>
              {pedido.deliveryType === 'DELIVERY'
                ? `Envío a domicilio — ${pedido.deliveryAddress}`
                : 'Recoge en tienda'}
            </span>
          </div>

          <hr className="border-gray-100" />

          {/* Items */}
          <div className="space-y-2">
            <p className="text-xs font-medium text-gray-400 uppercase tracking-wide">Productos</p>
            {pedido.items.map((item) => (
              <div key={item.id} className="flex justify-between text-sm">
                <span className="text-gray-700">{item.productName} <span className="text-gray-400">x{item.quantity}</span></span>
                <span className="font-medium text-gray-900">{formatearPrecio(item.subtotal)}</span>
              </div>
            ))}
          </div>

          <hr className="border-gray-100" />

          {/* Total */}
          <div className="flex justify-between font-bold text-gray-900">
            <span>Total</span>
            <span>{formatearPrecio(pedido.total)}</span>
          </div>
        </div>

        {/* Acciones */}
        <div className="mt-6 flex flex-col sm:flex-row gap-3">
          <Link
            href="/pedidos"
            className="flex-1 text-center py-3 px-4 rounded-xl border border-indigo-200 text-indigo-600 font-semibold text-sm hover:bg-indigo-50 transition-colors"
          >
            Ver mis pedidos
          </Link>
          <Link
            href="/catalogo"
            className="flex-1 text-center py-3 px-4 rounded-xl bg-indigo-600 text-white font-semibold text-sm hover:bg-indigo-700 transition-colors"
          >
            Seguir comprando
          </Link>
        </div>

      </div>
    </main>
  );
}

'use client';

import { useEffect, useState } from 'react';
import Image from 'next/image';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { useCarrito } from '@/context/CarritoContext';
import { useAuth } from '@/context/AuthContext';
import { api } from '@/lib/api';
import { Pedido, TipoEntrega } from '@/types';

const formatearPrecio = (precio: number) =>
  new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP', maximumFractionDigits: 0 }).format(precio);

interface AdvertenciaStock {
  productoId: string;
  nombre: string;
  cantidadAnterior: number;
  stockActual: number;
}

export default function CarritoPage() {
  const { items, totalItems, totalPrecio, modificarCantidad, eliminar, vaciar } = useCarrito();
  const { isAuthenticated, getToken } = useAuth();
  const router = useRouter();
  const [modalidad, setModalidad] = useState<TipoEntrega>('PICKUP');
  const [direccion, setDireccion] = useState('');
  const [advertencias, setAdvertencias] = useState<AdvertenciaStock[]>([]);
  const [validando, setValidando] = useState(false);
  const [confirmando, setConfirmando] = useState(false);
  const [errorConfirmar, setErrorConfirmar] = useState<string | null>(null);

  // Pre-rellenar dirección desde el perfil del usuario
  useEffect(() => {
    if (!isAuthenticated) return;
    const token = getToken();
    if (!token) return;
    api.get<{ address: string | null }>('/users/me', token)
      .then((data) => { if (data.address) setDireccion(data.address); })
      .catch(() => {});
  }, [isAuthenticated, getToken]);

  // RF-CR-05: Validar stock en tiempo real al montar la página (batch endpoint SS-29)
  useEffect(() => {
    if (items.length === 0) return;

    const validarStock = async () => {
      setValidando(true);
      try {
        const res = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/api/cart/validate`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(items.map((i) => ({ productId: i.producto.id, quantity: i.cantidad }))),
        });
        if (!res.ok) return;

        const data: { valid: boolean; items: { productId: string; productName: string; requestedQuantity: number; availableStock: number; available: boolean }[] } = await res.json();

        const nuevasAdvertencias: AdvertenciaStock[] = [];
        for (const result of data.items) {
          if (!result.available) {
            nuevasAdvertencias.push({
              productoId: result.productId,
              nombre: result.productName,
              cantidadAnterior: result.requestedQuantity,
              stockActual: result.availableStock,
            });
            if (result.availableStock === 0) {
              eliminar(result.productId);
            } else {
              modificarCantidad(result.productId, result.availableStock);
            }
          }
        }
        setAdvertencias(nuevasAdvertencias);
      } catch {
        // ignorar errores de red
      } finally {
        setValidando(false);
      }
    };

    validarStock();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  if (items.length === 0) {
    return (
      <main className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center px-4">
          <svg xmlns="http://www.w3.org/2000/svg" className="w-16 h-16 text-gray-300 mx-auto mb-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1}>
            <path strokeLinecap="round" strokeLinejoin="round" d="M2.25 3h1.386c.51 0 .955.343 1.087.835l.383 1.437M7.5 14.25a3 3 0 00-3 3h15.75m-12.75-3h11.218c1.121-2.3 2.1-4.684 2.943-7.154a60.477 60.477 0 00-16.536-1.84M7.5 14.25L5.106 5.272M6 20.25a.75.75 0 11-1.5 0 .75.75 0 011.5 0zm12.75 0a.75.75 0 11-1.5 0 .75.75 0 011.5 0z" />
          </svg>
          <h1 className="text-xl font-semibold text-gray-900 mb-2">Tu carrito esta vacio</h1>
          <p className="text-gray-500 mb-6">Agrega productos desde el catalogo para comenzar.</p>
          <Link
            href="/catalogo"
            className="inline-block px-6 py-3 bg-indigo-600 text-white rounded-xl font-medium hover:bg-indigo-700 transition-colors"
          >
            Ver catalogo
          </Link>
        </div>
      </main>
    );
  }

  const puedeConfirmar =
    !confirmando &&
    (modalidad === 'PICKUP' || (modalidad === 'DELIVERY' && direccion.trim().length > 0));

  const confirmarPedido = async () => {
    const token = getToken();
    if (!token) {
      router.push('/login');
      return;
    }
    setConfirmando(true);
    setErrorConfirmar(null);
    try {
      const pedido = await api.post<Pedido>(
        '/api/orders',
        {
          items: items.map((i) => ({ productId: i.producto.id, quantity: i.cantidad })),
          deliveryType: modalidad,
          deliveryAddress: modalidad === 'DELIVERY' ? direccion.trim() : undefined,
        },
        token
      );
      vaciar();
      sessionStorage.setItem('pedido_confirmacion', JSON.stringify(pedido));
      router.push('/checkout');
    } catch (err: unknown) {
      const msg = (err as { message?: string })?.message ?? 'Error al confirmar el pedido';
      setErrorConfirmar(msg);
    } finally {
      setConfirmando(false);
    }
  };

  return (
    <main className="min-h-screen bg-gray-50">
      <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <h1 className="text-2xl font-bold text-gray-900 mb-6">
          Carrito ({totalItems} {totalItems === 1 ? 'producto' : 'productos'})
        </h1>

        {/* Advertencias de stock */}
        {advertencias.length > 0 && (
          <div className="mb-6 space-y-2">
            {advertencias.map((adv) => (
              <div key={adv.productoId} className="flex items-start gap-2 bg-amber-50 border border-amber-200 rounded-lg px-4 py-3 text-sm text-amber-800">
                <span className="mt-0.5">⚠</span>
                <span>
                  <strong>{adv.nombre}</strong>:{' '}
                  {adv.stockActual === 0
                    ? 'se agoto y fue eliminado de tu carrito.'
                    : `el stock bajo a ${adv.stockActual} unidad${adv.stockActual !== 1 ? 'es' : ''} (tenias ${adv.cantidadAnterior}).`}
                </span>
              </div>
            ))}
          </div>
        )}

        {validando && (
          <p className="text-sm text-gray-400 mb-4 animate-pulse">Verificando disponibilidad...</p>
        )}

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">

          {/* Lista de items */}
          <div className="lg:col-span-2 space-y-3">
            {items.map((item) => (
              <div key={item.producto.id} className="bg-white rounded-2xl border border-gray-100 shadow-sm p-4 flex gap-4">
                {/* Imagen */}
                <div className="relative w-20 h-20 flex-shrink-0 rounded-xl overflow-hidden bg-gray-100">
                  {item.producto.imageUrl ? (
                    <Image
                      src={item.producto.imageUrl}
                      alt={item.producto.name}
                      fill
                      sizes="80px"
                      className="object-cover"
                    />
                  ) : (
                    <div className="w-full h-full flex items-center justify-center text-gray-300">
                      <svg xmlns="http://www.w3.org/2000/svg" className="w-8 h-8" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1} d="M9 19V6l12-3v13M9 19c0 1.105-1.343 2-3 2s-3-.895-3-2 1.343-2 3-2 3 .895 3 2zm12-3c0 1.105-1.343 2-3 2s-3-.895-3-2 1.343-2 3-2 3 .895 3 2zM9 10l12-3" />
                      </svg>
                    </div>
                  )}
                </div>

                {/* Info */}
                <div className="flex-1 min-w-0">
                  <p className="text-xs text-indigo-600 font-medium">{item.producto.genre}</p>
                  <p className="font-semibold text-gray-900 text-sm truncate">{item.producto.name}</p>
                  <p className="text-xs text-gray-400 mt-0.5">{formatearPrecio(item.producto.price)} c/u</p>

                  <div className="flex items-center justify-between mt-3">
                    {/* Controles cantidad */}
                    <div className="flex items-center gap-2">
                      <button
                        onClick={() => modificarCantidad(item.producto.id, item.cantidad - 1)}
                        className="w-7 h-7 rounded-full border border-gray-300 flex items-center justify-center text-gray-600 hover:border-indigo-400 hover:text-indigo-600 transition-colors text-lg leading-none"
                      >
                        −
                      </button>
                      <span className="w-6 text-center text-sm font-semibold">{item.cantidad}</span>
                      <button
                        onClick={() => modificarCantidad(item.producto.id, item.cantidad + 1)}
                        disabled={item.cantidad >= item.producto.stock}
                        className="w-7 h-7 rounded-full border border-gray-300 flex items-center justify-center text-gray-600 hover:border-indigo-400 hover:text-indigo-600 transition-colors disabled:opacity-30 disabled:cursor-not-allowed text-lg leading-none"
                      >
                        +
                      </button>
                    </div>

                    <div className="flex items-center gap-3">
                      <span className="font-bold text-gray-900 text-sm">
                        {formatearPrecio(item.producto.price * item.cantidad)}
                      </span>
                      <button
                        onClick={() => eliminar(item.producto.id)}
                        className="text-gray-300 hover:text-red-400 transition-colors"
                        aria-label="Eliminar"
                      >
                        <svg xmlns="http://www.w3.org/2000/svg" className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                          <path strokeLinecap="round" strokeLinejoin="round" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                        </svg>
                      </button>
                    </div>
                  </div>
                </div>
              </div>
            ))}

            <button
              onClick={vaciar}
              className="text-sm text-gray-400 hover:text-red-400 transition-colors mt-2"
            >
              Vaciar carrito
            </button>
          </div>

          {/* Panel resumen */}
          <div className="space-y-4">

            {/* RF-CR-06: Modalidad de entrega */}
            <div className="bg-white rounded-2xl border border-gray-100 shadow-sm p-5">
              <h2 className="font-semibold text-gray-900 mb-3">Modalidad de entrega</h2>
              <div className="space-y-2">
                {(['PICKUP', 'DELIVERY'] as TipoEntrega[]).map((tipo) => (
                  <label
                    key={tipo}
                    className={`flex items-start gap-3 p-3 rounded-xl border cursor-pointer transition-colors ${
                      modalidad === tipo ? 'border-indigo-500 bg-indigo-50' : 'border-gray-200 hover:border-gray-300'
                    }`}
                  >
                    <input
                      type="radio"
                      name="modalidad"
                      value={tipo}
                      checked={modalidad === tipo}
                      onChange={() => setModalidad(tipo)}
                      className="mt-0.5 accent-indigo-600"
                    />
                    <div>
                      <p className="font-medium text-sm text-gray-900">
                        {tipo === 'PICKUP' ? 'Recoger en tienda' : 'Envio a domicilio'}
                      </p>
                      <p className="text-xs text-gray-400">
                        {tipo === 'PICKUP' ? 'Retiras en nuestro punto de entrega' : 'Te lo llevamos a tu direccion'}
                      </p>
                    </div>
                  </label>
                ))}
              </div>

              {/* RF-CR-07: Campo direccion condicional */}
              {modalidad === 'DELIVERY' && (
                <div className="mt-4">
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Direccion de entrega <span className="text-red-500">*</span>
                  </label>
                  <textarea
                    value={direccion}
                    onChange={(e) => setDireccion(e.target.value)}
                    placeholder="Ej: Cra 15 #80-25, Apto 301, Bogota"
                    rows={3}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500 resize-none"
                  />
                </div>
              )}
            </div>

            {/* Resumen de costos */}
            <div className="bg-white rounded-2xl border border-gray-100 shadow-sm p-5">
              <h2 className="font-semibold text-gray-900 mb-4">Resumen</h2>

              <div className="space-y-2 text-sm">
                {items.map((item) => (
                  <div key={item.producto.id} className="flex justify-between text-gray-500">
                    <span className="truncate mr-2">{item.producto.name} x{item.cantidad}</span>
                    <span className="flex-shrink-0">{formatearPrecio(item.producto.price * item.cantidad)}</span>
                  </div>
                ))}
              </div>

              <div className="border-t border-gray-100 mt-4 pt-4 flex justify-between font-bold text-gray-900">
                <span>Total</span>
                <span>{formatearPrecio(totalPrecio)}</span>
              </div>

              {errorConfirmar && (
                <p className="mt-3 text-xs text-red-600 text-center">{errorConfirmar}</p>
              )}

              <button
                onClick={confirmarPedido}
                disabled={!puedeConfirmar}
                className="mt-4 w-full py-3 px-6 rounded-xl font-semibold text-sm transition-colors bg-indigo-600 text-white hover:bg-indigo-700 disabled:bg-gray-200 disabled:text-gray-400 disabled:cursor-not-allowed"
              >
                {confirmando ? 'Procesando...' : 'Confirmar pedido'}
              </button>

              {modalidad === 'DELIVERY' && !direccion.trim() && (
                <p className="mt-2 text-xs text-amber-600 text-center">Ingresa una direccion de entrega para continuar</p>
              )}
            </div>
          </div>
        </div>
      </div>
    </main>
  );
}

'use client';

import { useState } from 'react';
import { useCarrito } from '@/context/CarritoContext';
import { Producto } from '@/types';

interface BotonAgregarCarritoProps {
  producto: Producto;
}

export default function BotonAgregarCarrito({ producto }: BotonAgregarCarritoProps) {
  const { agregar, items } = useCarrito();
  const [cantidad, setCantidad] = useState(1);
  const [agregado, setAgregado] = useState(false);

  const itemEnCarrito = items.find((i) => i.producto.id === producto.id);
  const cantidadEnCarrito = itemEnCarrito?.cantidad ?? 0;
  const stockDisponible = producto.stock - cantidadEnCarrito;

  const handleAgregar = () => {
    if (stockDisponible <= 0) return;
    agregar(producto, cantidad);
    setAgregado(true);
    setCantidad(1);
    setTimeout(() => setAgregado(false), 1500);
  };

  if (producto.stock === 0) {
    return (
      <button disabled className="w-full py-3 px-6 rounded-xl font-semibold text-sm bg-gray-200 text-gray-400 cursor-not-allowed">
        No disponible
      </button>
    );
  }

  if (stockDisponible <= 0) {
    return (
      <button disabled className="w-full py-3 px-6 rounded-xl font-semibold text-sm bg-gray-200 text-gray-400 cursor-not-allowed">
        Máximo en carrito ({cantidadEnCarrito})
      </button>
    );
  }

  return (
    <div className="space-y-3">
      <div className="flex items-center gap-3">
        <span className="text-sm font-medium text-gray-700">Cantidad</span>
        <div className="flex items-center gap-2">
          <button
            onClick={() => setCantidad((c) => Math.max(1, c - 1))}
            className="w-8 h-8 rounded-full border border-gray-300 flex items-center justify-center text-gray-600 hover:border-indigo-400 hover:text-indigo-600 transition-colors text-lg leading-none"
          >
            −
          </button>
          <span className="w-8 text-center font-semibold text-gray-900">{cantidad}</span>
          <button
            onClick={() => setCantidad((c) => Math.min(stockDisponible, c + 1))}
            disabled={cantidad >= stockDisponible}
            className="w-8 h-8 rounded-full border border-gray-300 flex items-center justify-center text-gray-600 hover:border-indigo-400 hover:text-indigo-600 transition-colors disabled:opacity-30 disabled:cursor-not-allowed text-lg leading-none"
          >
            +
          </button>
        </div>
        {stockDisponible <= 5 && (
          <span className="text-xs text-amber-600 font-medium">Quedan {stockDisponible}</span>
        )}
      </div>

      <button
        onClick={handleAgregar}
        className={`w-full py-3 px-6 rounded-xl font-semibold text-sm transition-all ${
          agregado
            ? 'bg-green-500 text-white'
            : 'bg-indigo-600 text-white hover:bg-indigo-700'
        }`}
      >
        {agregado ? '¡Agregado al carrito!' : 'Agregar al carrito'}
      </button>
    </div>
  );
}

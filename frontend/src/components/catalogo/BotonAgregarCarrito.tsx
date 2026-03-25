'use client';

import { useState } from 'react';
import { useCarrito } from '@/context/CarritoContext';
import { Producto } from '@/types';

interface BotonAgregarCarritoProps {
  producto: Producto;
}

export default function BotonAgregarCarrito({ producto }: BotonAgregarCarritoProps) {
  const { agregar, items } = useCarrito();
  const [agregado, setAgregado] = useState(false);

  const itemEnCarrito = items.find((i) => i.producto.id === producto.id);
  const cantidadEnCarrito = itemEnCarrito?.cantidad ?? 0;
  const stockDisponible = producto.stock - cantidadEnCarrito;

  const handleAgregar = () => {
    if (stockDisponible <= 0) return;
    agregar(producto, 1);
    setAgregado(true);
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
        Maximo en carrito ({cantidadEnCarrito})
      </button>
    );
  }

  return (
    <button
      onClick={handleAgregar}
      className={`w-full py-3 px-6 rounded-xl font-semibold text-sm transition-all ${
        agregado
          ? 'bg-green-500 text-white'
          : 'bg-indigo-600 text-white hover:bg-indigo-700'
      }`}
    >
      {agregado ? 'Agregado al carrito' : 'Agregar al carrito'}
    </button>
  );
}

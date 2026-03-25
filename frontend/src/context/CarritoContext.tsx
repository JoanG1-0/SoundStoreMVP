'use client';

import { createContext, useContext, useEffect, useReducer } from 'react';
import { Producto } from '@/types';

// ── Tipos ──────────────────────────────────────────────────────────────────

export interface ItemCarrito {
  producto: Producto;
  cantidad: number;
}

interface EstadoCarrito {
  items: ItemCarrito[];
}

type AccionCarrito =
  | { type: 'AGREGAR'; producto: Producto; cantidad?: number }
  | { type: 'MODIFICAR_CANTIDAD'; productoId: string; cantidad: number }
  | { type: 'ELIMINAR'; productoId: string }
  | { type: 'VACIAR' }
  | { type: 'CARGAR'; items: ItemCarrito[] };

interface CarritoContextValue {
  items: ItemCarrito[];
  totalItems: number;
  totalPrecio: number;
  agregar: (producto: Producto, cantidad?: number) => void;
  modificarCantidad: (productoId: string, cantidad: number) => void;
  eliminar: (productoId: string) => void;
  vaciar: () => void;
}

// ── Reducer ────────────────────────────────────────────────────────────────

function carritoReducer(estado: EstadoCarrito, accion: AccionCarrito): EstadoCarrito {
  switch (accion.type) {
    case 'AGREGAR': {
      const cantidad = accion.cantidad ?? 1;
      const existente = estado.items.find((i) => i.producto.id === accion.producto.id);
      if (existente) {
        const nuevaCantidad = Math.min(existente.cantidad + cantidad, accion.producto.stock);
        return {
          items: estado.items.map((i) =>
            i.producto.id === accion.producto.id ? { ...i, cantidad: nuevaCantidad } : i
          ),
        };
      }
      return {
        items: [...estado.items, { producto: accion.producto, cantidad: Math.min(cantidad, accion.producto.stock) }],
      };
    }
    case 'MODIFICAR_CANTIDAD': {
      if (accion.cantidad <= 0) {
        return { items: estado.items.filter((i) => i.producto.id !== accion.productoId) };
      }
      return {
        items: estado.items.map((i) =>
          i.producto.id === accion.productoId
            ? { ...i, cantidad: Math.min(accion.cantidad, i.producto.stock) }
            : i
        ),
      };
    }
    case 'ELIMINAR':
      return { items: estado.items.filter((i) => i.producto.id !== accion.productoId) };
    case 'VACIAR':
      return { items: [] };
    case 'CARGAR':
      return { items: accion.items };
    default:
      return estado;
  }
}

// ── Context ────────────────────────────────────────────────────────────────

const CarritoContext = createContext<CarritoContextValue | null>(null);

const STORAGE_KEY = 'soundstore_carrito';

export function CarritoProvider({ children }: { children: React.ReactNode }) {
  const [estado, dispatch] = useReducer(carritoReducer, { items: [] });

  // Cargar desde localStorage al montar
  useEffect(() => {
    try {
      const guardado = localStorage.getItem(STORAGE_KEY);
      if (guardado) {
        const items: ItemCarrito[] = JSON.parse(guardado);
        dispatch({ type: 'CARGAR', items });
      }
    } catch {
      // ignorar errores de parseo
    }
  }, []);

  // Persistir en localStorage cuando cambia el carrito
  useEffect(() => {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(estado.items));
  }, [estado.items]);

  const totalItems = estado.items.reduce((acc, i) => acc + i.cantidad, 0);
  const totalPrecio = estado.items.reduce((acc, i) => acc + i.producto.price * i.cantidad, 0);

  const value: CarritoContextValue = {
    items: estado.items,
    totalItems,
    totalPrecio,
    agregar: (producto, cantidad) => dispatch({ type: 'AGREGAR', producto, cantidad }),
    modificarCantidad: (productoId, cantidad) => dispatch({ type: 'MODIFICAR_CANTIDAD', productoId, cantidad }),
    eliminar: (productoId) => dispatch({ type: 'ELIMINAR', productoId }),
    vaciar: () => dispatch({ type: 'VACIAR' }),
  };

  return <CarritoContext.Provider value={value}>{children}</CarritoContext.Provider>;
}

export function useCarrito(): CarritoContextValue {
  const ctx = useContext(CarritoContext);
  if (!ctx) throw new Error('useCarrito debe usarse dentro de CarritoProvider');
  return ctx;
}

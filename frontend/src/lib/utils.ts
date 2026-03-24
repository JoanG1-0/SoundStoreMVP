import { EstadoPedido, TipoEntrega } from '@/types';

export function formatearPrecio(valor: number): string {
  return new Intl.NumberFormat('es-CO', {
    style: 'currency',
    currency: 'COP',
    minimumFractionDigits: 0,
  }).format(valor);
}

export function formatearFecha(fechaIso: string): string {
  return new Intl.DateTimeFormat('es-CO', {
    dateStyle: 'medium',
    timeStyle: 'short',
  }).format(new Date(fechaIso));
}

export const ETIQUETA_ESTADO: Record<EstadoPedido, string> = {
  PENDING: 'Pendiente',
  CONFIRMED: 'Confirmado',
  PREPARING: 'En preparacion',
  READY_PICKUP: 'Listo para recoger',
  ON_THE_WAY: 'En camino',
  DELIVERED: 'Entregado',
  CANCELLED: 'Cancelado',
};

export const ETIQUETA_ENTREGA: Record<TipoEntrega, string> = {
  DELIVERY: 'Domicilio',
  PICKUP: 'Recoger en tienda',
};

import Image from 'next/image';
import Link from 'next/link';
import { Producto } from '@/types';

interface TarjetaProductoProps {
  producto: Producto;
}

const formatearPrecio = (precio: number) =>
  new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP', maximumFractionDigits: 0 }).format(precio);

export default function TarjetaProducto({ producto }: TarjetaProductoProps) {
  return (
    <Link
      href={`/catalogo/${producto.id}`}
      className="group flex flex-col bg-white rounded-2xl overflow-hidden shadow-sm hover:shadow-md transition-shadow border border-gray-100"
    >
      <div className="relative aspect-square bg-gray-100">
        {producto.imageUrl ? (
          <Image
            src={producto.imageUrl}
            alt={producto.name}
            fill
            sizes="(max-width: 640px) 50vw, (max-width: 1024px) 33vw, 25vw"
            className="object-cover group-hover:scale-105 transition-transform duration-300"
          />
        ) : (
          <div className="absolute inset-0 flex items-center justify-center text-gray-300">
            <svg xmlns="http://www.w3.org/2000/svg" className="w-16 h-16" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1} d="M9 19V6l12-3v13M9 19c0 1.105-1.343 2-3 2s-3-.895-3-2 1.343-2 3-2 3 .895 3 2zm12-3c0 1.105-1.343 2-3 2s-3-.895-3-2 1.343-2 3-2 3 .895 3 2zM9 10l12-3" />
            </svg>
          </div>
        )}
      </div>

      <div className="flex flex-col flex-1 p-3 sm:p-4 gap-1">
        <span className="text-[10px] sm:text-xs font-medium text-indigo-600 uppercase tracking-wide truncate">{producto.genre}</span>
        <h3 className="font-semibold text-gray-900 line-clamp-2 text-xs sm:text-sm leading-snug">{producto.name}</h3>
        <div className="mt-auto pt-2 sm:pt-3 flex items-end justify-between gap-1">
          <span className="text-sm sm:text-base font-bold text-gray-900 leading-tight">{formatearPrecio(producto.price)}</span>
          {producto.stock <= 5 && (
            <span className="text-[10px] sm:text-xs text-amber-600 font-medium shrink-0">Quedan {producto.stock}</span>
          )}
        </div>
      </div>
    </Link>
  );
}

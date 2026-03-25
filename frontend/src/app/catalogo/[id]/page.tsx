import Image from 'next/image';
import Link from 'next/link';
import { notFound } from 'next/navigation';
import { Producto } from '@/types';

interface Props {
  params: Promise<{ id: string }>;
}

async function getProducto(id: string): Promise<Producto | null> {
  const res = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/products/${id}`, {
    cache: 'no-store',
  });
  if (res.status === 404) return null;
  if (!res.ok) throw new Error('Error al obtener el producto');
  return res.json();
}

const formatearPrecio = (precio: number) =>
  new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP', maximumFractionDigits: 0 }).format(precio);

export default async function ProductoDetallePage({ params }: Props) {
  const { id } = await params;
  const producto = await getProducto(id);

  if (!producto) notFound();

  return (
    <main className="min-h-screen bg-gray-50">
      <div className="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8 py-8">

        {/* Breadcrumb */}
        <nav className="mb-6 text-sm text-gray-500">
          <Link href="/catalogo" className="hover:text-indigo-600 transition-colors">
            Catalogo
          </Link>
          <span className="mx-2">/</span>
          <span className="text-gray-900">{producto.name}</span>
        </nav>

        <div className="bg-white rounded-2xl shadow-sm border border-gray-100 overflow-hidden">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-0">

            {/* Imagen */}
            <div className="relative aspect-square bg-gray-100">
              {producto.imageUrl ? (
                <Image
                  src={producto.imageUrl}
                  alt={producto.name}
                  fill
                  sizes="(max-width: 768px) 100vw, 50vw"
                  className="object-cover"
                  priority
                />
              ) : (
                <div className="absolute inset-0 flex items-center justify-center text-gray-300">
                  <svg xmlns="http://www.w3.org/2000/svg" className="w-24 h-24" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1} d="M9 19V6l12-3v13M9 19c0 1.105-1.343 2-3 2s-3-.895-3-2 1.343-2 3-2 3 .895 3 2zm12-3c0 1.105-1.343 2-3 2s-3-.895-3-2 1.343-2 3-2 3 .895 3 2zM9 10l12-3" />
                  </svg>
                </div>
              )}
            </div>

            {/* Info */}
            <div className="flex flex-col p-6 sm:p-8 gap-4">
              <div>
                <span className="text-xs font-semibold text-indigo-600 uppercase tracking-wide">
                  {producto.genre}
                </span>
                <h1 className="mt-1 text-2xl sm:text-3xl font-bold text-gray-900 leading-tight">
                  {producto.name}
                </h1>
              </div>

              <p className="text-3xl font-bold text-gray-900">
                {formatearPrecio(producto.price)}
              </p>

              <p className="text-gray-600 leading-relaxed text-sm sm:text-base">
                {producto.description}
              </p>

              {/* Stock */}
              <div className="flex items-center gap-2">
                {producto.stock > 5 ? (
                  <span className="inline-flex items-center gap-1 text-sm text-green-700 font-medium">
                    <span className="w-2 h-2 rounded-full bg-green-500 inline-block" />
                    En stock ({producto.stock} disponibles)
                  </span>
                ) : producto.stock > 0 ? (
                  <span className="inline-flex items-center gap-1 text-sm text-amber-600 font-medium">
                    <span className="w-2 h-2 rounded-full bg-amber-400 inline-block" />
                    Ultimas {producto.stock} unidades
                  </span>
                ) : (
                  <span className="inline-flex items-center gap-1 text-sm text-red-600 font-medium">
                    <span className="w-2 h-2 rounded-full bg-red-500 inline-block" />
                    Agotado
                  </span>
                )}
              </div>

              {/* Boton agregar al carrito */}
              <button
                disabled={producto.stock === 0}
                className="mt-auto w-full py-3 px-6 rounded-xl font-semibold text-sm transition-colors
                  bg-indigo-600 text-white hover:bg-indigo-700 disabled:bg-gray-200 disabled:text-gray-400 disabled:cursor-not-allowed"
              >
                {producto.stock === 0 ? 'No disponible' : 'Agregar al carrito'}
              </button>

              <Link
                href="/catalogo"
                className="text-center text-sm text-gray-400 hover:text-indigo-600 transition-colors"
              >
                Volver al catalogo
              </Link>
            </div>
          </div>
        </div>
      </div>
    </main>
  );
}

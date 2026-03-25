import Link from 'next/link';

export default function ProductoNoEncontrado() {
  return (
    <main className="min-h-screen bg-gray-50 flex items-center justify-center">
      <div className="text-center px-4">
        <p className="text-6xl font-bold text-indigo-600 mb-4">404</p>
        <h1 className="text-xl font-semibold text-gray-900 mb-2">Producto no encontrado</h1>
        <p className="text-gray-500 mb-6">Este producto no existe o ya no esta disponible.</p>
        <Link
          href="/catalogo"
          className="inline-block px-6 py-3 bg-indigo-600 text-white rounded-xl font-medium hover:bg-indigo-700 transition-colors"
        >
          Volver al catalogo
        </Link>
      </div>
    </main>
  );
}

import { Producto } from '@/types';
import FiltroGenero from '@/components/catalogo/FiltroGenero';
import TarjetaProducto from '@/components/catalogo/TarjetaProducto';

interface SearchParams {
  genre?: string;
  search?: string;
}

async function getProductos(genre?: string, search?: string): Promise<Producto[]> {
  const params = new URLSearchParams();
  if (search) params.set('search', search);
  else if (genre) params.set('genre', genre);

  const url = `${process.env.NEXT_PUBLIC_API_URL}/products${params.toString() ? `?${params}` : ''}`;

  const res = await fetch(url, { cache: 'no-store' });
  if (!res.ok) return [];
  return res.json();
}

export default async function CatalogoPage({
  searchParams,
}: {
  searchParams: Promise<SearchParams>;
}) {
  const { genre, search } = await searchParams;

  const [todosLosProductos, productosFiltrados] = await Promise.all([
    getProductos(),
    genre || search ? getProductos(genre, search) : Promise.resolve(null),
  ]);

  const productos = productosFiltrados ?? todosLosProductos;
  const generos = [...new Set(todosLosProductos.map((p) => p.genre))].sort();

  return (
    <main className="min-h-screen bg-gray-50">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">

        {/* Cabecera */}
        <div className="mb-6">
          <h1 className="text-2xl sm:text-3xl font-bold text-gray-900">Catalogo</h1>
          <p className="text-gray-500 mt-1">
            {productos.length === 0
              ? 'No se encontraron productos'
              : `${productos.length} producto${productos.length !== 1 ? 's' : ''} disponible${productos.length !== 1 ? 's' : ''}`}
          </p>
        </div>

        {/* Barra de busqueda */}
        <form method="GET" action="/catalogo" className="mb-6">
          <div className="flex gap-2 w-full sm:max-w-md">
            <input
              type="text"
              name="search"
              defaultValue={search ?? ''}
              placeholder="Buscar por nombre o genero..."
              className="flex-1 px-4 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
            />
            <button
              type="submit"
              className="px-4 py-2 bg-indigo-600 text-white rounded-lg text-sm font-medium hover:bg-indigo-700 transition-colors"
            >
              Buscar
            </button>
          </div>
        </form>

        {/* Filtros de genero */}
        <div className="mb-8">
          <FiltroGenero generos={generos} generoActivo={genre} search={search} />
        </div>

        {/* Grid de productos */}
        {productos.length === 0 ? (
          <div className="text-center py-20 text-gray-400">
            <p className="text-lg">No hay productos que coincidan con tu busqueda.</p>
          </div>
        ) : (
          <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 gap-4 sm:gap-6">
            {productos.map((producto) => (
              <TarjetaProducto key={producto.id} producto={producto} />
            ))}
          </div>
        )}
      </div>
    </main>
  );
}

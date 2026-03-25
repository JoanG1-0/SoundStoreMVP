import Link from 'next/link';

interface FiltroGeneroProps {
  generos: string[];
  generoActivo?: string;
  search?: string;
}

export default function FiltroGenero({ generos, generoActivo, search }: FiltroGeneroProps) {
  const buildHref = (genre?: string) => {
    const params = new URLSearchParams();
    if (genre) params.set('genre', genre);
    if (search) params.set('search', search);
    const qs = params.toString();
    return `/catalogo${qs ? `?${qs}` : ''}`;
  };

  return (
    <div className="flex flex-wrap gap-2">
      <Link
        href={buildHref()}
        className={`px-4 py-1.5 rounded-full text-sm font-medium transition-colors ${
          !generoActivo
            ? 'bg-indigo-600 text-white'
            : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
        }`}
      >
        Todos
      </Link>
      {generos.map((genero) => (
        <Link
          key={genero}
          href={buildHref(genero)}
          className={`px-4 py-1.5 rounded-full text-sm font-medium transition-colors ${
            generoActivo === genero
              ? 'bg-indigo-600 text-white'
              : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
          }`}
        >
          {genero}
        </Link>
      ))}
    </div>
  );
}

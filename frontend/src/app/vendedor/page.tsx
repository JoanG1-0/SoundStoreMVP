'use client';

import { useEffect } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import { useAuth } from '@/context/AuthContext';

export default function VendedorDashboardPage() {
  const { isAuthenticated } = useAuth();
  const router = useRouter();

  useEffect(() => {
    if (!isAuthenticated) router.replace('/login');
  }, [isAuthenticated, router]);

  return (
    <main className="min-h-screen bg-gray-50">
      <div className="max-w-4xl mx-auto px-4 sm:px-6 py-10">

        <div className="mb-8">
          <h1 className="text-2xl font-bold text-gray-900">Panel de vendedor</h1>
          <p className="text-sm text-gray-400 mt-1">Gestiona tus productos y los pedidos activos</p>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <Link href="/vendedor/productos"
            className="bg-white border border-gray-100 rounded-2xl p-6 hover:border-indigo-200 hover:shadow-sm transition-all flex items-start gap-4">
            <span className="p-3 bg-indigo-50 rounded-xl text-indigo-600 shrink-0">
              <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2}
                  d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4" />
              </svg>
            </span>
            <div>
              <p className="font-semibold text-gray-900">Mis productos</p>
              <p className="text-sm text-gray-400 mt-0.5">Crea, edita y activa o desactiva tu catálogo</p>
            </div>
          </Link>

          <Link href="/vendedor/pedidos"
            className="bg-white border border-gray-100 rounded-2xl p-6 hover:border-indigo-200 hover:shadow-sm transition-all flex items-start gap-4">
            <span className="p-3 bg-amber-50 rounded-xl text-amber-600 shrink-0">
              <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2}
                  d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
              </svg>
            </span>
            <div>
              <p className="font-semibold text-gray-900">Pedidos activos</p>
              <p className="text-sm text-gray-400 mt-0.5">Revisa y avanza el estado de cada pedido</p>
            </div>
          </Link>
        </div>

      </div>
    </main>
  );
}

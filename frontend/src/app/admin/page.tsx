'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import { useAuth } from '@/context/AuthContext';
import { api } from '@/lib/api';
import { MetricasDashboard } from '@/types';

const formatearPrecio = (valor: number) =>
  new Intl.NumberFormat('es-CO', {
    style: 'currency',
    currency: 'COP',
    maximumFractionDigits: 0,
  }).format(valor);

interface TarjetaProps {
  titulo: string;
  valor: string;
  descripcion: string;
  color: string;
  icono: React.ReactNode;
  alerta?: boolean;
}

function Tarjeta({ titulo, valor, descripcion, color, icono, alerta }: TarjetaProps) {
  return (
    <div className={`bg-white rounded-2xl border shadow-sm p-6 flex flex-col gap-3 ${alerta ? 'border-red-200' : 'border-gray-100'}`}>
      <div className="flex items-center justify-between">
        <span className="text-xs font-semibold text-gray-400 uppercase tracking-wide">{titulo}</span>
        <span className={`p-2 rounded-xl ${color}`}>{icono}</span>
      </div>
      <p className={`text-3xl font-bold ${alerta ? 'text-red-600' : 'text-gray-900'}`}>{valor}</p>
      <p className="text-xs text-gray-400">{descripcion}</p>
    </div>
  );
}

export default function AdminDashboardPage() {
  const { isAuthenticated, getToken } = useAuth();
  const router = useRouter();
  const [metricas, setMetricas] = useState<MetricasDashboard | null>(null);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!isAuthenticated) {
      router.replace('/login');
      return;
    }
    const token = getToken();
    if (!token) return;

    api.get<MetricasDashboard>('/admin/dashboard', token)
      .then(setMetricas)
      .catch(() => setError('No se pudieron cargar las métricas del dashboard'))
      .finally(() => setCargando(false));
  }, [isAuthenticated, getToken, router]);

  if (cargando) {
    return (
      <main className="min-h-screen bg-gray-50 flex items-center justify-center">
        <p className="text-gray-400 animate-pulse">Cargando dashboard...</p>
      </main>
    );
  }

  if (error || !metricas) {
    return (
      <main className="min-h-screen bg-gray-50 flex items-center justify-center">
        <p className="text-red-500">{error ?? 'Error inesperado'}</p>
      </main>
    );
  }

  const hoy = new Date().toLocaleDateString('es-CO', {
    weekday: 'long', day: 'numeric', month: 'long', year: 'numeric',
  });

  return (
    <main className="min-h-screen bg-gray-50">
      <div className="max-w-5xl mx-auto px-4 sm:px-6 py-10">

        {/* Encabezado */}
        <div className="mb-8">
          <h1 className="text-2xl font-bold text-gray-900">Panel de administración</h1>
          <p className="text-sm text-gray-400 mt-1 capitalize">{hoy}</p>
        </div>

        {/* Tarjetas de métricas */}
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 mb-10">
          <Tarjeta
            titulo="Pedidos hoy"
            valor={String(metricas.pedidosDelDia)}
            descripcion="Total de pedidos creados hoy"
            color="bg-indigo-50 text-indigo-600"
            icono={
              <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2}
                  d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
              </svg>
            }
          />
          <Tarjeta
            titulo="Pendientes"
            valor={String(metricas.pedidosPendientes)}
            descripcion="Pedidos esperando confirmación"
            color="bg-amber-50 text-amber-600"
            icono={
              <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2}
                  d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
            }
          />
          <Tarjeta
            titulo="Stock bajo"
            valor={String(metricas.productosStockBajo)}
            descripcion="Productos activos con stock < 5"
            color="bg-red-50 text-red-600"
            alerta={metricas.productosStockBajo > 0}
            icono={
              <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2}
                  d="M12 9v2m0 4h.01M10.29 3.86L1.82 18a2 2 0 001.71 3h16.94a2 2 0 001.71-3L13.71 3.86a2 2 0 00-3.42 0z" />
              </svg>
            }
          />
          <Tarjeta
            titulo="Ventas del mes"
            valor={formatearPrecio(metricas.ventasDelMes)}
            descripcion="Total facturado este mes"
            color="bg-green-50 text-green-600"
            icono={
              <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2}
                  d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
            }
          />
        </div>

        {/* Accesos rápidos */}
        <div>
          <h2 className="text-sm font-semibold text-gray-500 uppercase tracking-wide mb-3">Accesos rápidos</h2>
          <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
            <Link href="/admin/pedidos"
              className="bg-white border border-gray-100 rounded-xl p-4 hover:border-indigo-200 hover:shadow-sm transition-all flex items-center gap-3">
              <span className="p-2 bg-indigo-50 rounded-lg text-indigo-600">
                <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2}
                    d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
                </svg>
              </span>
              <span className="text-sm font-medium text-gray-700">Gestionar pedidos</span>
            </Link>
            <Link href="/admin/productos"
              className="bg-white border border-gray-100 rounded-xl p-4 hover:border-indigo-200 hover:shadow-sm transition-all flex items-center gap-3">
              <span className="p-2 bg-indigo-50 rounded-lg text-indigo-600">
                <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2}
                    d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4" />
                </svg>
              </span>
              <span className="text-sm font-medium text-gray-700">Gestionar productos</span>
            </Link>
            <Link href="/admin/usuarios"
              className="bg-white border border-gray-100 rounded-xl p-4 hover:border-indigo-200 hover:shadow-sm transition-all flex items-center gap-3">
              <span className="p-2 bg-indigo-50 rounded-lg text-indigo-600">
                <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2}
                    d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0z" />
                </svg>
              </span>
              <span className="text-sm font-medium text-gray-700">Gestionar usuarios</span>
            </Link>
          </div>
        </div>

      </div>
    </main>
  );
}

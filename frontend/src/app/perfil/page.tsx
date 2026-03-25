'use client';

import { useState } from 'react';
import Link from 'next/link';

export default function PerfilPage() {
  const [editando, setEditando] = useState(false);

  return (
    <main className="min-h-screen bg-gray-50">
      <div className="max-w-2xl mx-auto px-4 sm:px-6 lg:px-8 py-8">

        <div className="mb-6">
          <h1 className="text-2xl font-bold text-gray-900">Mi perfil</h1>
          <p className="text-gray-500 text-sm mt-1">Gestiona tus datos personales</p>
        </div>

        {/* Aviso de autenticacion pendiente */}
        <div className="bg-amber-50 border border-amber-200 rounded-xl px-4 py-3 mb-6 text-sm text-amber-800">
          Esta pagina requiere iniciar sesion. El formulario estara disponible cuando se implemente el modulo de autenticacion.
        </div>

        <div className="bg-white rounded-2xl border border-gray-100 shadow-sm p-6 space-y-5">
          <div className="flex items-center justify-between">
            <h2 className="font-semibold text-gray-900">Datos personales</h2>
            <button
              onClick={() => setEditando(!editando)}
              className="text-sm text-indigo-600 hover:text-indigo-700 font-medium transition-colors"
            >
              {editando ? 'Cancelar' : 'Editar'}
            </button>
          </div>

          {!editando ? (
            <div className="space-y-4">
              {[
                { label: 'Nombre completo', value: '—' },
                { label: 'Correo electronico', value: '—' },
                { label: 'Telefono', value: '—' },
                { label: 'Direccion', value: '—' },
              ].map(({ label, value }) => (
                <div key={label}>
                  <p className="text-xs text-gray-400 font-medium uppercase tracking-wide">{label}</p>
                  <p className="text-gray-700 mt-0.5">{value}</p>
                </div>
              ))}
            </div>
          ) : (
            <form className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Nombre completo</label>
                <input
                  type="text"
                  disabled
                  placeholder="Requiere autenticacion"
                  className="w-full px-3 py-2 border border-gray-200 rounded-lg text-sm bg-gray-50 text-gray-400 cursor-not-allowed"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Telefono</label>
                <input
                  type="text"
                  disabled
                  placeholder="Requiere autenticacion"
                  className="w-full px-3 py-2 border border-gray-200 rounded-lg text-sm bg-gray-50 text-gray-400 cursor-not-allowed"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Direccion de entrega</label>
                <textarea
                  disabled
                  placeholder="Requiere autenticacion"
                  rows={3}
                  className="w-full px-3 py-2 border border-gray-200 rounded-lg text-sm bg-gray-50 text-gray-400 cursor-not-allowed resize-none"
                />
              </div>
              <button
                type="submit"
                disabled
                className="w-full py-2.5 rounded-xl bg-gray-200 text-gray-400 text-sm font-semibold cursor-not-allowed"
              >
                Guardar cambios
              </button>
            </form>
          )}
        </div>

        <div className="mt-4 text-center">
          <Link href="/catalogo" className="text-sm text-gray-400 hover:text-indigo-600 transition-colors">
            Volver al catalogo
          </Link>
        </div>
      </div>
    </main>
  );
}

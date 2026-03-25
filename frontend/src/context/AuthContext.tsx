'use client';

import { createContext, useContext, useEffect, useState } from 'react';
import { Rol } from '@/types';

// ── Tipos ──────────────────────────────────────────────────────────────────

interface SesionAuth {
  accessToken: string;
  refreshToken: string;
  role: Rol;
}

interface AuthContextValue {
  sesion: SesionAuth | null;
  isAuthenticated: boolean;
  role: Rol | null;
  login: (data: SesionAuth) => void;
  logout: () => void;
  getToken: () => string | null;
}

// ── Context ────────────────────────────────────────────────────────────────

const AuthContext = createContext<AuthContextValue | null>(null);

const KEYS = {
  accessToken: 'soundstore_access_token',
  refreshToken: 'soundstore_refresh_token',
  role: 'soundstore_role',
} as const;

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [sesion, setSesion] = useState<SesionAuth | null>(null);
  const [listo, setListo] = useState(false);

  // Rehidratar desde localStorage al montar (solo en cliente)
  useEffect(() => {
    const accessToken = localStorage.getItem(KEYS.accessToken);
    const refreshToken = localStorage.getItem(KEYS.refreshToken);
    const role = localStorage.getItem(KEYS.role) as Rol | null;

    if (accessToken && refreshToken && role) {
      setSesion({ accessToken, refreshToken, role });
    }
    setListo(true);
  }, []);

  const login = (data: SesionAuth) => {
    localStorage.setItem(KEYS.accessToken, data.accessToken);
    localStorage.setItem(KEYS.refreshToken, data.refreshToken);
    localStorage.setItem(KEYS.role, data.role);
    setSesion(data);
  };

  const logout = () => {
    localStorage.removeItem(KEYS.accessToken);
    localStorage.removeItem(KEYS.refreshToken);
    localStorage.removeItem(KEYS.role);
    setSesion(null);
  };

  const getToken = () => sesion?.accessToken ?? null;

  const value: AuthContextValue = {
    sesion,
    isAuthenticated: !!sesion,
    role: sesion?.role ?? null,
    login,
    logout,
    getToken,
  };

  // No renderizar hijos hasta que se haya leído localStorage (evita flash)
  if (!listo) return null;

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth debe usarse dentro de AuthProvider');
  return ctx;
}

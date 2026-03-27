const BASE_URL = process.env.NEXT_PUBLIC_API_URL;

const STORAGE_KEYS = {
  accessToken: 'soundstore_access_token',
  refreshToken: 'soundstore_refresh_token',
  role: 'soundstore_role',
} as const;

type HttpMethod = 'GET' | 'POST' | 'PUT' | 'PATCH' | 'DELETE';

interface RequestOptions {
  method?: HttpMethod;
  body?: unknown;
  token?: string;
}

async function tryRefresh(): Promise<string | null> {
  if (typeof window === 'undefined') return null;
  const refreshToken = localStorage.getItem(STORAGE_KEYS.refreshToken);
  if (!refreshToken) return null;

  try {
    const res = await fetch(`${BASE_URL}/auth/refresh`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ refreshToken }),
    });
    if (!res.ok) return null;
    const data = await res.json();
    localStorage.setItem(STORAGE_KEYS.accessToken, data.accessToken);
    localStorage.setItem(STORAGE_KEYS.refreshToken, data.refreshToken);
    return data.accessToken as string;
  } catch {
    return null;
  }
}

function clearSession(): void {
  if (typeof window === 'undefined') return;
  localStorage.removeItem(STORAGE_KEYS.accessToken);
  localStorage.removeItem(STORAGE_KEYS.refreshToken);
  localStorage.removeItem(STORAGE_KEYS.role);
  window.location.href = '/login';
}

async function request<T>(endpoint: string, options: RequestOptions = {}): Promise<T> {
  const { method = 'GET', body, token } = options;

  const headers: HeadersInit = { 'Content-Type': 'application/json' };
  if (token) headers['Authorization'] = `Bearer ${token}`;

  const response = await fetch(`${BASE_URL}${endpoint}`, {
    method,
    headers,
    body: body ? JSON.stringify(body) : undefined,
  });

  if (response.status === 401 && token) {
    const newToken = await tryRefresh();
    if (!newToken) {
      clearSession();
      throw { message: 'Sesión expirada. Por favor inicia sesión de nuevo.', status: 401 };
    }
    const retry = await fetch(`${BASE_URL}${endpoint}`, {
      method,
      headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${newToken}` },
      body: body ? JSON.stringify(body) : undefined,
    });
    if (!retry.ok) {
      const err = await retry.json().catch(() => ({ message: 'Error desconocido' }));
      throw { message: err.message ?? 'Error en la solicitud', status: retry.status };
    }
    if (retry.status === 204) return undefined as T;
    return retry.json();
  }

  if (!response.ok) {
    const error = await response.json().catch(() => ({ message: 'Error desconocido' }));
    throw { message: error.message ?? 'Error en la solicitud', status: response.status };
  }

  if (response.status === 204) return undefined as T;

  return response.json();
}

export const api = {
  get: <T>(endpoint: string, token?: string) =>
    request<T>(endpoint, { method: 'GET', token }),

  post: <T>(endpoint: string, body: unknown, token?: string) =>
    request<T>(endpoint, { method: 'POST', body, token }),

  put: <T>(endpoint: string, body: unknown, token?: string) =>
    request<T>(endpoint, { method: 'PUT', body, token }),

  patch: <T>(endpoint: string, body: unknown, token?: string) =>
    request<T>(endpoint, { method: 'PATCH', body, token }),

  delete: <T>(endpoint: string, token?: string) =>
    request<T>(endpoint, { method: 'DELETE', token }),

  downloadCsv: async (endpoint: string, filename: string, token: string): Promise<void> => {
    const headers: HeadersInit = { 'Authorization': `Bearer ${token}` };
    const response = await fetch(`${BASE_URL}${endpoint}`, { headers });
    if (!response.ok) {
      throw { message: 'No se pudo generar el reporte', status: response.status };
    }
    const blob = await response.blob();
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = filename;
    a.click();
    URL.revokeObjectURL(url);
  },
};

// ========================
// Enums
// ========================

export type Rol = 'BUYER' | 'SELLER' | 'ADMIN';

export type EstadoPedido =
  | 'PENDING'
  | 'CONFIRMED'
  | 'PREPARING'
  | 'READY_PICKUP'
  | 'ON_THE_WAY'
  | 'DELIVERED'
  | 'CANCELLED';

export type TipoEntrega = 'DELIVERY' | 'PICKUP';

export type TipoOtp = 'REGISTRATION' | 'PASSWORD_RESET';

// ========================
// Entidades
// ========================

export interface Usuario {
  id: string;
  fullName: string;
  email: string;
  phone: string;
  address?: string;
  role: Rol;
  isActive: boolean;
  emailVerified: boolean;
}

export interface Producto {
  id: string;
  name: string;
  description: string;
  price: number;
  genre: string;
  stock: number;
  imageUrl?: string;
  active: boolean;
  createdBy: string;
  createdAt: string;
  updatedAt: string;
}

export interface ItemPedido {
  id: string;
  productId: string;
  productName: string;
  quantity: number;
  unitPrice: number;
  subtotal: number;
}

export interface Pedido {
  id: string;
  orderNumber: string;
  userId: string;
  status: EstadoPedido;
  deliveryType: TipoEntrega;
  deliveryAddress?: string;
  total: number;
  items: ItemPedido[];
  createdAt: string;
}

// ========================
// Auth
// ========================

export interface AuthTokens {
  accessToken: string;
  refreshToken: string;
}

export interface SesionUsuario extends Usuario {
  tokens: AuthTokens;
}

// ========================
// Admin
// ========================

export interface MetricasDashboard {
  pedidosDelDia: number;
  pedidosPendientes: number;
  productosStockBajo: number;
  ventasDelMes: number;
}

// ========================
// API — Respuestas genericas
// ========================

export interface ApiResponse<T> {
  data: T;
  message?: string;
}

export interface ApiError {
  message: string;
  status: number;
}

export interface Pagina<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

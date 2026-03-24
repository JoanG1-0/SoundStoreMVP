# SoundStore MVP

## Conexion Jira
- Sitio: https://analisis-datos-financieros.atlassian.net
- Proyecto: SoundStore MVP
- Clave del proyecto: SS

## Instrucciones de flujo de trabajo
Antes de escribir cualquier codigo debes:
1. Leer la tarea asignada directamente desde Jira usando su clave (ej: SS-13)
2. Entender la descripcion, area, estimacion y RFs asociados
3. Actualizar el estado de la tarea a "En progreso" al comenzar
4. Implementar siguiendo el stack, convenciones y documentacion de este archivo
5. Verificar que la tarea cumple todos los criterios de la Definicion de Done
6. Actualizar el estado a "Hecho" en Jira al terminar

## Como buscar tareas en Jira
- Por clave:   "Lee la tarea SS-13"
- Por sprint:  "Lista las tareas pendientes del Sprint 1"
- Por epica:   "Muestrame las tareas de la epica EPIC-02"

---

## Documentacion completa del proyecto
Antes de implementar cualquier modulo, lee el documento correspondiente en la carpeta /docs:

| Documento | Cuando leerlo |
|---|---|
| `docs/SoundStore_SRS_v1.3.md` | Antes de implementar cualquier requerimiento funcional o no funcional |
| `docs/SoundStore_ERD_v1.0.md` | Antes de crear cualquier entidad, repositorio o migracion de BD |
| `docs/SoundStore_PlanQA_v1.0.md` | Antes de escribir pruebas. Contiene los casos de prueba por modulo |
| `docs/SoundStore_PlanProyecto_v1.0.md` | Para entender el alcance de cada sprint y las estimaciones |
| `docs/SoundStore_PlanDespliegue_v1.0.md` | Antes de tocar variables de entorno, CI/CD o cualquier tarea de despliegue |
| `docs/SoundStore_ProjectCharter_v1.0.md` | Para entender el alcance general, restricciones y lo que esta fuera del MVP |

---

## Stack tecnologico
- **Backend:** Spring Boot 3.x / Java 17 — arquitectura estricta Controller -> Service -> Repository
- **Frontend:** Next.js 14+ / React 18
- **Base de datos:** PostgreSQL 15+ en Railway (ORM: Spring Data JPA / Hibernate)
- **Autenticacion:** JWT + Spring Security + RBAC (roles: BUYER, SELLER, ADMIN)
- **Imagenes:** Cloudinary
- **Email:** SendGrid
- **Deploy:** Vercel (frontend) + Railway (backend + BD)
- **CI/CD:** GitHub Actions
- **Gestion:** Jira (Scrum, sprints semanales)

---

## Convenciones de codigo

### General
- Variables de entorno: **nunca hardcodeadas**, siempre en `.env` y en Railway/Vercel (RNF-MA-03)
- Idioma de la UI y mensajes de error: **espanol (Colombia)** (RNF-US-03, RNF-US-04)
- Ramas Git: `feature/*` -> `develop` (staging) -> `main` (produccion)

### Backend (Spring Boot)
- Arquitectura en capas: **Controller -> Service -> Repository**. Sin saltarse capas.
- Contrasenas: **BCrypt costo minimo 10** (RNF-SE-07). Nunca texto plano.
- IDs: **UUID v4** en todas las entidades, nunca enteros auto-incrementales
- Tokens JWT: expiracion configurable. Refresh token: maximo 7 dias (RNF-SE-02)
- RBAC: ningun usuario accede a rutas fuera de su rol (RNF-SE-03)
- Validacion y sanitizacion de entradas contra SQL injection y XSS (RNF-SE-04)
- Rate limiting OTP: maximo 3 intentos por email en 10 minutos (RNF-SE-05)

### Frontend (Next.js)
- Responsive obligatorio: funcional desde 320px en movil, tablet y desktop (RNF-US-01)
- Flujo de compra completo en maximo 5 pasos (RNF-US-02)
- Variables expuestas al browser deben tener prefijo `NEXT_PUBLIC_`
- Nunca colocar claves secretas en variables `NEXT_PUBLIC_`

---

## Resumen del ERD (entidades y campos clave)
> Detalle completo con restricciones y decisiones de diseno en `docs/SoundStore_ERD_v1.0.md`

### `users`
| Campo | Tipo | Notas |
|---|---|---|
| id | UUID PK | UUID v4, generado automaticamente |
| full_name | VARCHAR(120) | NOT NULL |
| email | VARCHAR(255) | NOT NULL, UNIQUE |
| password_hash | VARCHAR(255) | NOT NULL — BCrypt costo 10 |
| phone | VARCHAR(20) | NOT NULL |
| address | TEXT | NULLABLE — obligatorio al primer pedido |
| role | ENUM | `BUYER` / `SELLER` / `ADMIN` |
| is_active | BOOLEAN | Default: true |
| email_verified | BOOLEAN | Default: false — cambia a true tras OTP |

### `products`
| Campo | Tipo | Notas |
|---|---|---|
| id | UUID PK | UUID v4 |
| name | VARCHAR(150) | NOT NULL |
| description | TEXT | NOT NULL |
| price | DECIMAL(10,2) | NOT NULL — CHECK(price > 0) |
| genre | VARCHAR(80) | NOT NULL — texto libre |
| stock | INTEGER | NOT NULL — CHECK(stock >= 0), default 0 |
| image_url | VARCHAR(500) | NULLABLE — URL de Cloudinary |
| is_active | BOOLEAN | Default: true |
| created_by | UUID FK -> users | NOT NULL — ON DELETE RESTRICT |

### `orders`
| Campo | Tipo | Notas |
|---|---|---|
| id | UUID PK | UUID v4 |
| order_number | VARCHAR(20) | UNIQUE — formato: SS-20260001 |
| user_id | UUID FK -> users | NOT NULL — ON DELETE RESTRICT |
| status | ENUM | `PENDING` / `CONFIRMED` / `PREPARING` / `READY_PICKUP` / `ON_THE_WAY` / `DELIVERED` / `CANCELLED` |
| delivery_type | ENUM | `DELIVERY` / `PICKUP` |
| delivery_address | TEXT | NULLABLE — snapshot, obligatorio si DELIVERY |
| total | DECIMAL(12,2) | NOT NULL — CHECK(total >= 0) |

### `order_items`
| Campo | Tipo | Notas |
|---|---|---|
| id | UUID PK | UUID v4 |
| order_id | UUID FK -> orders | NOT NULL — ON DELETE CASCADE |
| product_id | UUID FK -> products | NOT NULL — ON DELETE RESTRICT |
| quantity | INTEGER | NOT NULL — CHECK(quantity > 0) |
| unit_price | DECIMAL(10,2) | NOT NULL — SNAPSHOT del precio al momento del pedido |
| subtotal | DECIMAL(12,2) | NOT NULL — quantity x unit_price, persistido |

### `otp_codes`
| Campo | Tipo | Notas |
|---|---|---|
| id | UUID PK | UUID v4 |
| email | VARCHAR(255) | NOT NULL — sin FK a users |
| code | VARCHAR(6) | NOT NULL — 6 digitos numericos |
| type | ENUM | `REGISTRATION` / `PASSWORD_RESET` |
| used | BOOLEAN | Default: false |
| expires_at | TIMESTAMP | NOT NULL — created_at + 5 minutos |

---

## Flujo de estados del pedido

```
PENDING -> CONFIRMED -> PREPARING -> ON_THE_WAY   (solo DELIVERY) -> DELIVERED
                                  -> READY_PICKUP  (solo PICKUP)   -> DELIVERED

Cancelacion: solo desde PENDING o CONFIRMED
Al cancelar: restituir stock automaticamente (RF-PE-08)
```

---

## Requerimientos funcionales del MVP (prioridad Alta)
> Detalle completo en `docs/SoundStore_SRS_v1.3.md`

### Autenticacion
- RF-AU-01: Registro con email + contrasena + OTP de verificacion (6 digitos, 5 min, uso unico)
- RF-AU-02: Captura de datos faltantes al primer pedido
- RF-AU-03: Login con email + contrasena (sin OTP en cada acceso)
- RF-AU-03B: Recuperacion de contrasena con OTP
- RF-AU-04: Cierre de sesion
- RF-AU-05: Admin crea cuentas de Vendedor y Admin con contrasena temporal
- RF-AU-06: Admin activa/desactiva cualquier cuenta
- RF-AU-07: JWT + RBAC (3 roles)

### Catalogo
- RF-CA-01: Catalogo con todos los atributos del producto
- RF-CA-02: Filtro por genero musical
- RF-CA-03: Vista de detalle del producto
- RF-CA-04: Ocultar productos con stock=0 o inactivos
- RF-CA-05: Crear producto (Vendedor/Admin) con imagen
- RF-CA-06: Editar producto
- RF-CA-07: Activar/desactivar producto

### Carrito
- RF-CR-01 a RF-CR-07: Agregar, modificar, eliminar productos. Resumen. Validacion stock. Seleccion modalidad de entrega. Confirmar direccion.

### Pedidos
- RF-PE-01 a RF-PE-09: Creacion, descuento de stock, ciclo de vida, notificaciones email, historial, listado activos, actualizacion de estado, restitucion de stock al cancelar.

### Panel Admin
- RF-AD-01: Dashboard con metricas (pedidos del dia, pendientes, stock bajo <5, ventas del mes)
- RF-AD-02: Gestion completa de productos
- RF-AD-03: Gestion completa de pedidos
- RF-AD-04: Gestion de usuarios

---

## Definicion de Done (DoD)
Una tarea solo pasa a "Hecho" en Jira si cumple **todos** estos criterios:

1. Codigo empujado a la rama `develop` en GitHub
2. Pruebas unitarias escritas y en verde (JUnit 5 + Mockito)
3. Cobertura JaCoCo >= 70% en la capa de servicios
4. Endpoints probados manualmente con Postman
5. Sin defectos de severidad Critica o Alta abiertos
6. Responsive verificado en movil y desktop
7. Mensajes de error en espanol y claros para el usuario final

---

## Lo que esta FUERA del MVP (no implementar)
- Pasarela de pago en linea (PSP) — el pago es contra entrega o en efectivo
- Aplicacion movil nativa (iOS / Android)
- Integracion con sistemas de logistica de terceros
- Personalizacion del contenido de las memorias USB por el comprador
- Modulo de descuentos o cupones
- Pruebas E2E automatizadas (Cypress / Playwright)
- Expansion a mercados fuera de Colombia

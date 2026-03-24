# Diseño de Base de Datos — ERD SoundStore
*Portal de Venta de Memorias USB con Música*

| Campo | Valor |
|---|---|
| **Cliente** | Pikiña |
| **Proyecto** | SoundStore |
| **Versión del Documento** | v1.0 |
| **Referencia SRS** | SoundStore SRS v1.3 |
| **Motor de Base de Datos** | PostgreSQL 15+ |
| **Fecha de Elaboración** | Marzo 2026 |
| **Estado** | Borrador — Pendiente de Aprobación |

---

## 1. Introducción

### 1.1 Propósito

El presente documento describe el diseño de la base de datos del sistema SoundStore. Define las entidades, sus atributos, tipos de datos, restricciones de integridad y las relaciones entre ellas, siguiendo el modelo relacional implementado en PostgreSQL 15+.

Este documento es la base técnica para el desarrollo del backend (Spring Boot + Spring Data JPA / Hibernate).

### 1.2 Convenciones del Documento

| Convención | Descripción |
|---|---|
| **PK** | Primary Key — Clave primaria de la tabla. |
| **FK** | Foreign Key — Clave foránea que referencia a otra tabla. |
| **UUID** | Universally Unique Identifier — Identificador único generado automáticamente por el sistema. |
| **ENUM** | Tipo enumerado — Solo acepta los valores definidos en la lista. |
| **NOT NULL** | Campo obligatorio — No puede estar vacío. |
| **NULLABLE** | Campo opcional — Puede ser nulo. |
| **snapshot** | Valor copiado en el momento de la transacción. No se actualiza aunque cambie el origen. |

---

## 2. Entidades del Sistema

El sistema SoundStore está compuesto por **5 entidades principales**: `users`, `products`, `orders`, `order_items`, `otp_codes`.

### 2.1 Tabla: `users`

Almacena todos los usuarios del sistema, independientemente de su rol (Comprador, Vendedor o Administrador). El campo `role` discrimina el tipo de usuario y determina sus permisos.

| Campo | Tipo | PK / FK | Nullable | Descripción / Restricción |
|---|---|---|---|---|
| **id** | UUID | **PK** | NO | Identificador único. Generado automáticamente (UUID v4). |
| **full_name** | VARCHAR(120) | — | NO | Nombre completo del usuario. |
| **email** | VARCHAR(255) | — | NO | Correo electrónico. Único en el sistema (UNIQUE constraint). |
| **password_hash** | VARCHAR(255) | — | NO | Contraseña hasheada con BCrypt (costo mínimo 10). Nunca texto plano. |
| **phone** | VARCHAR(20) | — | NO | Número de teléfono. Requerido para todos los roles. |
| **address** | TEXT | — | SÍ | Dirección de entrega principal. Obligatoria para Compradores al primer pedido. |
| **role** | ENUM | — | NO | Valores: `BUYER`, `SELLER`, `ADMIN`. Determina los permisos del usuario. |
| **is_active** | BOOLEAN | — | NO | Default: `true`. El Admin puede desactivar cualquier cuenta. |
| **email_verified** | BOOLEAN | — | NO | Default: `false`. Cambia a `true` tras validar el OTP de registro. |
| **created_at** | TIMESTAMP | — | NO | Fecha y hora de creación. Seteada automáticamente por el sistema. |
| **updated_at** | TIMESTAMP | — | NO | Fecha y hora de última modificación. Actualizada automáticamente. |

**Restricciones adicionales:**
- `UNIQUE(email)`: no pueden existir dos usuarios con el mismo correo electrónico.
- `CHECK(role IN ('BUYER','SELLER','ADMIN'))`: solo se aceptan los tres roles definidos.
- El primer Administrador se crea mediante configuración inicial del sistema (seed).

---

### 2.2 Tabla: `products`

Almacena el catálogo de memorias USB disponibles. El campo `is_active` controla la visibilidad en el catálogo público.

| Campo | Tipo | PK / FK | Nullable | Descripción / Restricción |
|---|---|---|---|---|
| **id** | UUID | **PK** | NO | Identificador único. Generado automáticamente (UUID v4). |
| **name** | VARCHAR(150) | — | NO | Nombre o título de la memoria USB. |
| **description** | TEXT | — | NO | Descripción del contenido musical de la memoria. |
| **price** | DECIMAL(10,2) | — | NO | Precio de venta. Debe ser mayor que 0. |
| **genre** | VARCHAR(80) | — | NO | Género musical. Campo de texto libre definido por el Vendedor o Admin. |
| **stock** | INTEGER | — | NO | Unidades disponibles. Default: 0. No puede ser negativo. |
| **image_url** | VARCHAR(500) | — | SÍ | URL de la imagen almacenada en Cloudinary. |
| **is_active** | BOOLEAN | — | NO | Default: `true`. Productos inactivos no aparecen en el catálogo público. |
| **created_by** | UUID | **FK → users(id)** | NO | Usuario (Vendedor o Admin) que creó el producto. |
| **created_at** | TIMESTAMP | — | NO | Fecha y hora de creación. |
| **updated_at** | TIMESTAMP | — | NO | Fecha y hora de última modificación. |

**Restricciones adicionales:**
- `CHECK(price > 0)`: el precio debe ser estrictamente mayor que cero.
- `CHECK(stock >= 0)`: el stock no puede ser negativo.
- `FK created_by → users(id) ON DELETE RESTRICT`: no se puede eliminar un usuario que haya creado productos.
- Productos con `stock = 0` o `is_active = false` son ocultados del catálogo por lógica de aplicación (RF-CA-04).

---

### 2.3 Tabla: `orders`

Almacena la cabecera de cada pedido. El campo `delivery_address` es un **snapshot** de la dirección al momento del pedido.

| Campo | Tipo | PK / FK | Nullable | Descripción / Restricción |
|---|---|---|---|---|
| **id** | UUID | **PK** | NO | Identificador único. Generado automáticamente (UUID v4). |
| **order_number** | VARCHAR(20) | — | NO | Número de orden legible. Único. Formato: `SS-20260001`. |
| **user_id** | UUID | **FK → users(id)** | NO | Comprador que realizó el pedido. |
| **status** | ENUM | — | NO | Estados: `PENDING`, `CONFIRMED`, `PREPARING`, `READY_PICKUP`, `ON_THE_WAY`, `DELIVERED`, `CANCELLED`. |
| **delivery_type** | ENUM | — | NO | Valores: `DELIVERY` (domicilio), `PICKUP` (recogida en punto físico). |
| **delivery_address** | TEXT | — | SÍ | Snapshot de la dirección de entrega. Obligatorio si `delivery_type = DELIVERY`. Nulo si es `PICKUP`. |
| **total** | DECIMAL(12,2) | — | NO | Total del pedido. Calculado como suma de subtotales de `order_items`. |
| **created_at** | TIMESTAMP | — | NO | Fecha y hora de creación del pedido. |
| **updated_at** | TIMESTAMP | — | NO | Fecha y hora del último cambio de estado. |

**Restricciones adicionales:**
- `UNIQUE(order_number)`: cada pedido tiene un número de orden único.
- `CHECK(total >= 0)`: el total no puede ser negativo.
- `FK user_id → users(id) ON DELETE RESTRICT`: no se puede eliminar un usuario con pedidos.
- Estado inicial al crearse: `PENDING` (RF-PE-01).
- Transiciones permitidas: `PENDING → CONFIRMED → PREPARING → ON_THE_WAY/READY_PICKUP → DELIVERED`. Cancelación solo desde `PENDING` o `CONFIRMED`.

---

### 2.4 Tabla: `order_items`

Almacena el detalle de cada producto incluido en un pedido. El campo `unit_price` es un **snapshot** del precio en el momento de la compra.

| Campo | Tipo | PK / FK | Nullable | Descripción / Restricción |
|---|---|---|---|---|
| **id** | UUID | **PK** | NO | Identificador único. Generado automáticamente (UUID v4). |
| **order_id** | UUID | **FK → orders(id)** | NO | Pedido al que pertenece este ítem. |
| **product_id** | UUID | **FK → products(id)** | NO | Producto incluido en el pedido. |
| **quantity** | INTEGER | — | NO | Cantidad de unidades solicitadas. Debe ser mayor que 0. |
| **unit_price** | DECIMAL(10,2) | — | NO | Snapshot del precio unitario al momento de confirmar el pedido. |
| **subtotal** | DECIMAL(12,2) | — | NO | Calculado: `quantity × unit_price`. Persistido para integridad histórica. |

**Restricciones adicionales:**
- `CHECK(quantity > 0)`: la cantidad debe ser al menos 1.
- `CHECK(unit_price > 0)`: el precio unitario debe ser mayor que cero.
- `FK order_id → orders(id) ON DELETE CASCADE`: si se elimina un pedido, sus ítems se eliminan también.
- `FK product_id → products(id) ON DELETE RESTRICT`: no se puede eliminar un producto con ítems de pedido.
- ⚠️ **DECISIÓN CRÍTICA**: `unit_price` NO referencia el precio actual de `products.price`. Es un valor copiado e inmutable.

---

### 2.5 Tabla: `otp_codes`

Almacena los códigos OTP para verificación de correo al registro y para recuperación de contraseña. Vigencia: 5 minutos. Uso único.

| Campo | Tipo | PK / FK | Nullable | Descripción / Restricción |
|---|---|---|---|---|
| **id** | UUID | **PK** | NO | Identificador único. Generado automáticamente (UUID v4). |
| **email** | VARCHAR(255) | — | NO | Correo electrónico al que fue enviado el OTP. |
| **code** | VARCHAR(6) | — | NO | Código OTP de 6 dígitos numéricos. |
| **type** | ENUM | — | NO | Valores: `REGISTRATION`, `PASSWORD_RESET`. |
| **used** | BOOLEAN | — | NO | Default: `false`. Cambia a `true` una vez que el código fue validado exitosamente. |
| **expires_at** | TIMESTAMP | — | NO | Timestamp de expiración. Generado como `created_at + 5 minutos`. |
| **created_at** | TIMESTAMP | — | NO | Fecha y hora de generación del código. |

**Restricciones adicionales:**
- Un código es válido solo si: `used = false AND expires_at > NOW()`.
- Rate limiting: máximo 3 códigos OTP por email en un periodo de 10 minutos (RNF-SE-05). Controlado en capa de aplicación.
- ⚠️ Esta tabla **NO tiene FK a users** porque el OTP de registro se genera antes de que el usuario exista en la tabla `users`.

---

## 3. Relaciones entre Entidades

| Relación | Cardinalidad | Tipo | Descripción |
|---|---|---|---|
| **users → orders** | 1 : N | FK | Un usuario (Comprador) puede tener muchos pedidos. Cada pedido pertenece a un solo usuario. |
| **orders → order_items** | 1 : N | FK + CASCADE | Un pedido contiene uno o más ítems. Si se elimina el pedido, sus ítems se eliminan en cascada. |
| **products → order_items** | 1 : N | FK | Un producto puede estar en muchos ítems de pedido (historial de ventas). |
| **users → products** | 1 : N | FK | Un usuario (Vendedor o Admin) puede crear múltiples productos. |
| **users → otp_codes** | 1 : N | Sin FK | Un email puede generar múltiples OTPs. Sin FK porque el OTP de registro se genera antes del usuario. |

---

## 4. Decisiones de Diseño

### 4.1 UUID como clave primaria
Se utilizan UUIDs (UUID v4) en todas las tablas en lugar de enteros auto-incrementales, por seguridad (IDs no predecibles), generación sin consultar BD y escalabilidad futura.

### 4.2 Snapshot de precio en `order_items`
`unit_price` almacena una copia del precio al momento del pedido. Garantiza que el historial de compras y reportes sean históricamente precisos e inmutables.

### 4.3 Snapshot de dirección en `orders`
`delivery_address` almacena una copia de la dirección al momento del pedido. Los pedidos anteriores no se ven afectados si el comprador cambia su dirección.

### 4.4 Tabla unificada de usuarios (`users`)
Una única tabla con campo `role` (ENUM) en lugar de tablas separadas por rol. Simplifica la autenticación (un endpoint de login), las consultas (sin JOINs entre roles) y el mantenimiento.

### 4.5 Género musical como texto libre
`genre` es `VARCHAR(80)` en lugar de una tabla separada. Responde al tamaño del MVP y a la necesidad de simplicidad operativa. Puede normalizarse en versiones futuras.

### 4.6 `otp_codes` sin FK a `users`
Permite que el OTP de registro sea generado antes de que el usuario exista en la BD, evitando estados transaccionales complejos.

---

## 5. Índices Recomendados

| Tabla | Campo(s) | Justificación |
|---|---|---|
| users | **email** | Búsqueda en login y verificación OTP. Consulta muy frecuente. |
| products | **is_active, stock** | Filtro del catálogo público: solo productos activos con stock > 0. |
| products | **genre** | Filtro por género musical (RF-CA-02). |
| orders | **user_id** | Historial de pedidos de un comprador (RF-PE-05). |
| orders | **status** | Listado de pedidos activos para Vendedor/Admin (RF-PE-06). |
| orders | **order_number** | Búsqueda por número de orden. UNIQUE implica índice automático. |
| order_items | **order_id** | Recuperar todos los ítems de un pedido. |
| otp_codes | **email, type, used** | Validación de OTP: código activo y no expirado por email y tipo. |

---

## 6. Tipos Enumerados (ENUM)

### 6.1 `users.role`

| Valor | Descripción |
|---|---|
| **BUYER** | Comprador. Auto-registrado. Puede navegar el catálogo y realizar pedidos. |
| **SELLER** | Vendedor. Creado por Admin. Gestiona el catálogo y actualiza estados de pedidos. |
| **ADMIN** | Administrador. Control total del sistema. Creado por otro Admin o seed inicial. |

### 6.2 `orders.status`

| Valor | Aplica a | Descripción |
|---|---|---|
| **PENDING** | Ambas rutas | Estado inicial. Pedido creado, pendiente de confirmación. |
| **CONFIRMED** | Ambas rutas | El negocio aceptó el pedido. Inicia preparación. |
| **PREPARING** | Ambas rutas | El pedido está siendo preparado y empacado. |
| **ON_THE_WAY** | Solo DELIVERY | El pedido fue entregado al mensajero y está en camino. |
| **READY_PICKUP** | Solo PICKUP | El pedido está listo para ser recogido en el punto físico. |
| **DELIVERED** | Ambas rutas | Estado final exitoso. |
| **CANCELLED** | Ambas rutas | Pedido cancelado. Solo desde PENDING o CONFIRMED. Restituye stock. |

### 6.3 `orders.delivery_type`

| Valor | Descripción |
|---|---|
| **DELIVERY** | Envío a domicilio. El campo `delivery_address` es obligatorio. |
| **PICKUP** | Recogida en punto físico. El campo `delivery_address` es nulo. |

### 6.4 `otp_codes.type`

| Valor | Descripción |
|---|---|
| **REGISTRATION** | OTP generado durante el registro para verificar el correo electrónico. |
| **PASSWORD_RESET** | OTP generado durante la recuperación de contraseña. |

---

## 7. Control de Versiones del Documento

| Versión | Fecha | Autor | Descripción del Cambio |
|---|---|---|---|
| v1.0 | Marzo 2026 | Equipo de Desarrollo | Versión inicial. 5 entidades definidas: users, products, orders, order_items, otp_codes. |

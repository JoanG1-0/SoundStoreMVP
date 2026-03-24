# Software Requirements Specification — SoundStore
*Portal de Venta de Memorias USB con Música*

| Campo | Valor |
|---|---|
| **Cliente** | Pikiña |
| **Proyecto** | SoundStore |
| **Versión del Documento** | v1.3 |
| **Estándar de Referencia** | IEEE 830 / ISO/IEC 25010 |
| **Fecha de Elaboración** | Marzo 2026 |
| **Estado** | Borrador — Pendiente de Aprobación |

---

## 1. Introducción

### 1.1 Propósito

El presente documento especifica los requerimientos funcionales y no funcionales del sistema SoundStore, un portal web de tipo marketplace destinado a la venta de memorias USB cargadas con música para el cliente Pikiña, con sede en Armenia, Quindío, Colombia.

Este SRS está dirigido al equipo de desarrollo externo (outsourcing), al cliente Pikiña y a cualquier stakeholder técnico involucrado en el ciclo de vida del software. Sirve como contrato técnico entre las partes y como base para el diseño, desarrollo y validación del sistema.

### 1.2 Alcance del Sistema

SoundStore es una aplicación web responsive que permite a los compradores adquirir memorias USB con música predefinida de forma remota, eligiendo entre recibir su pedido a domicilio o recogerlo en el punto físico del negocio. El sistema contempla tres tipos de usuarios: Comprador, Vendedor y Administrador, cada uno con funcionalidades diferenciadas.

El sistema **NO incluye** integración con pasarelas de pago en línea en esta versión (MVP), ni aplicación móvil nativa, ni personalización del contenido musical por parte del comprador.

### 1.3 Definiciones, Acrónimos y Abreviaturas

| Término / Acrónimo | Definición |
|---|---|
| **MVP** | Minimum Viable Product — Producto mínimo viable con las funcionalidades esenciales. |
| **SRS** | Software Requirements Specification — Especificación de Requerimientos de Software. |
| **RF** | Requerimiento Funcional — Describe una funcionalidad o comportamiento del sistema. |
| **RNF** | Requerimiento No Funcional — Describe una característica de calidad del sistema. |
| **Admin** | Administrador del sistema — Usuario con acceso total a la plataforma. |
| **Pick Up** | Modalidad de entrega donde el comprador recoge el pedido en el punto físico. |
| **PSP** | Payment Service Provider — Pasarela de pago. No aplica en esta versión del MVP. |
| **OTP** | One-Time Password — Código de verificación de un solo uso enviado al correo electrónico. |
| **ERD** | Entity Relationship Diagram — Diagrama de entidad-relación de la base de datos. |

### 1.4 Referencias

- Project Charter SoundStore v1.0 — Marzo 2026.
- IEEE Std 830-1998 — IEEE Recommended Practice for Software Requirements Specifications.
- ISO/IEC 25010:2011 — Modelo de calidad del producto de software.

---

## 2. Descripción General del Sistema

### 2.1 Perspectiva del Producto

SoundStore es un sistema nuevo, independiente, desarrollado como una aplicación web que opera en la nube. No es parte de un sistema mayor existente. Interactúa con los siguientes sistemas externos:

- Servicio de correo electrónico (SMTP / SendGrid o similar) para el envío de notificaciones automáticas a compradores.
- Servicio de almacenamiento de imágenes para las fotos de los productos (Cloudinary recomendado).
- Plataforma de despliegue: Vercel (frontend Next.js) + Railway (backend Spring Boot + PostgreSQL).

### 2.2 Funciones Principales del Sistema

| Módulo | Funciones Principales |
|---|---|
| **Autenticación** | Registro con email y contraseña, OTP de verificación único, inicio de sesión, recuperación de contraseña, gestión de sesiones y roles. |
| **Catálogo** | Listado de memorias USB, filtro por género musical, detalle de producto con atributos completos. |
| **Carrito** | Agregar/quitar productos, ver resumen de compra, selección de modalidad de entrega. |
| **Pedidos** | Creación, seguimiento y ciclo de vida completo del pedido con notificaciones por email. |
| **Panel Admin** | Dashboard, gestión de productos, pedidos, usuarios, historial y reportes de ventas. |
| **Panel Vendedor** | Gestión de catálogo propio y actualización del estado de pedidos. |

### 2.3 Tipos de Usuarios y Características

| Rol | Descripción | Forma de Acceso | Nivel Técnico Esperado |
|---|---|---|---|
| **Comprador** | Cliente final que navega el catálogo, agrega productos al carrito y realiza pedidos. | Registro con correo electrónico y contraseña. OTP al correo una única vez para verificar la cuenta. Auto-registro. | Básico. Solo necesita un smartphone o computador con internet. |
| **Vendedor** | Operador del negocio. Gestiona el catálogo y actualiza el estado de los pedidos. | Creado por el Administrador desde el panel interno. | Medio. Personal del punto físico capacitado para usar el panel. |
| **Administrador** | Control total del sistema. Gestiona usuarios, productos, pedidos, configuración y reportes. | Creado por el Administrador (primer Admin: configuración inicial del sistema). | Alto. Perfil técnico o dueño del negocio con capacitación previa. |

### 2.4 Restricciones Generales

- El sistema se desarrollará con el stack: Next.js (Frontend) + Spring Boot / Java (Backend) + PostgreSQL (Base de datos).
- El despliegue se realizará en Vercel (frontend) y Railway (backend + BD).
- El sistema operará exclusivamente en Colombia y en idioma español.
- No se integrará pasarela de pago en esta versión del MVP.
- El sistema debe ser responsive: funcional en dispositivos móviles, tablets y desktop.
- No habrá módulo de descuentos ni cupones en el MVP.

---

## 3. Requerimientos Funcionales

> Notación: `RF-[MÓDULO]-[NRO]`. Prioridad: **Alta** (imprescindible para MVP) / **Media** (importante pero diferible) / **Baja** (mejora futura).

### 3.1 Módulo de Autenticación y Gestión de Usuarios

| ID | Descripción | Actor | Prioridad |
|---|---|---|---|
| **RF-AU-01** | El sistema permitirá el registro de un Comprador mediante nombre completo, correo electrónico y contraseña. Al registrarse, se enviará un código OTP de 6 dígitos al correo electrónico para verificar que la cuenta es válida. Esta verificación se realiza una única vez. El código tendrá una vigencia de 5 minutos. | Comprador | **Alta** |
| **RF-AU-02** | Los datos requeridos del Comprador para procesar un pedido son: nombre completo, correo electrónico, número de teléfono y dirección de entrega. Si alguno falta al momento del primer pedido, el sistema lo solicitará antes de confirmar la orden. | Comprador | **Alta** |
| **RF-AU-03** | El sistema permitirá el inicio de sesión del Comprador mediante correo electrónico y contraseña. No se requerirá OTP en cada acceso, garantizando un ingreso rápido y sin fricción. | Comprador | **Alta** |
| **RF-AU-03B** | El sistema contará con un flujo de recuperación de contraseña: el Comprador ingresa su correo, recibe un OTP de 6 dígitos con vigencia de 5 minutos, lo valida y establece una nueva contraseña. | Comprador | **Alta** |
| **RF-AU-04** | El sistema permitirá al Comprador cerrar sesión desde cualquier dispositivo. | Comprador | **Alta** |
| **RF-AU-05** | El Administrador podrá crear cuentas de Vendedor y Administrador desde el panel interno, asignando nombre, número de teléfono, correo electrónico, contraseña temporal y rol. El usuario deberá cambiar la contraseña en su primer acceso. | Admin | **Alta** |
| **RF-AU-06** | El Administrador podrá activar o desactivar cualquier cuenta de usuario del sistema. | Admin | **Alta** |
| **RF-AU-07** | El sistema gestionará sesiones mediante tokens JWT con expiración configurable. Cada rol tendrá acceso únicamente a las rutas correspondientes a sus permisos. | Sistema | **Alta** |
| **RF-AU-08** | El Comprador podrá editar su perfil: nombre completo, correo electrónico, dirección de entrega y número de teléfono. | Comprador | **Media** |

### 3.2 Módulo de Catálogo de Productos

| ID | Descripción | Actor | Prioridad |
|---|---|---|---|
| **RF-CA-01** | El sistema mostrará un catálogo de memorias USB con los atributos: nombre/título, precio, descripción del contenido musical, imagen del producto, género musical y stock disponible. | Comprador | **Alta** |
| **RF-CA-02** | El sistema permitirá filtrar el catálogo por género musical. | Comprador | **Alta** |
| **RF-CA-03** | El sistema mostrará una vista de detalle de cada producto con todos sus atributos. | Comprador | **Alta** |
| **RF-CA-04** | El sistema ocultará del catálogo público los productos con stock igual a cero o en estado inactivo. | Sistema | **Alta** |
| **RF-CA-05** | El Vendedor y el Administrador podrán crear nuevos productos con todos sus atributos, incluyendo carga de imagen. | Vendedor / Admin | **Alta** |
| **RF-CA-06** | El Vendedor y el Administrador podrán editar los atributos de un producto existente. | Vendedor / Admin | **Alta** |
| **RF-CA-07** | El Vendedor y el Administrador podrán activar o desactivar un producto. Un producto desactivado no aparecerá en el catálogo público. | Vendedor / Admin | **Alta** |
| **RF-CA-08** | El sistema permitirá buscar productos por nombre o género musical mediante un campo de búsqueda en el catálogo. | Comprador | **Media** |

### 3.3 Módulo de Carrito de Compras

| ID | Descripción | Actor | Prioridad |
|---|---|---|---|
| **RF-CR-01** | El sistema permitirá al Comprador agregar uno o varios productos al carrito desde el catálogo o desde la vista de detalle del producto. | Comprador | **Alta** |
| **RF-CR-02** | El sistema permitirá al Comprador modificar la cantidad de unidades de cada producto en el carrito. | Comprador | **Alta** |
| **RF-CR-03** | El sistema permitirá al Comprador eliminar productos individuales del carrito. | Comprador | **Alta** |
| **RF-CR-04** | El carrito mostrará un resumen con: nombre del producto, cantidad, precio unitario y total de la compra. | Comprador | **Alta** |
| **RF-CR-05** | El sistema validará que la cantidad solicitada no supere el stock disponible del producto al momento de agregar al carrito. | Sistema | **Alta** |
| **RF-CR-06** | El Comprador deberá seleccionar la modalidad de entrega (Domicilio o Pick Up) antes de confirmar el pedido. | Comprador | **Alta** |
| **RF-CR-07** | Si la modalidad elegida es Domicilio, el sistema solicitará confirmación de la dirección de entrega del comprador. | Comprador | **Alta** |

### 3.4 Módulo de Pedidos

| ID | Descripción | Actor | Prioridad |
|---|---|---|---|
| **RF-PE-01** | Al confirmar el carrito, el sistema creará un pedido con estado inicial `PENDING` y generará un número de orden único. | Sistema | **Alta** |
| **RF-PE-02** | El sistema descontará automáticamente las unidades vendidas del stock de cada producto al confirmarse el pedido. | Sistema | **Alta** |
| **RF-PE-03** | El ciclo de vida de un pedido seguirá los estados: `PENDING → CONFIRMED → PREPARING → [READY_PICKUP / ON_THE_WAY] → DELIVERED`. También podrá pasar a `CANCELLED` desde `PENDING` o `CONFIRMED`. | Sistema / Admin / Vendedor | **Alta** |
| **RF-PE-04** | El sistema enviará un correo electrónico automático al Comprador en cada cambio de estado del pedido, indicando el estado actual y el número de orden. | Sistema | **Alta** |
| **RF-PE-05** | El Comprador podrá consultar el historial y estado actual de todos sus pedidos desde su perfil. | Comprador | **Alta** |
| **RF-PE-06** | El Vendedor y el Administrador podrán ver el listado de todos los pedidos activos y su estado actual. | Vendedor / Admin | **Alta** |
| **RF-PE-07** | El Vendedor y el Administrador podrán actualizar manualmente el estado de un pedido al estado siguiente permitido en el flujo. | Vendedor / Admin | **Alta** |
| **RF-PE-08** | Si un pedido es cancelado, el sistema restituirá el stock de los productos involucrados. | Sistema | **Alta** |
| **RF-PE-09** | Cada pedido almacenará: número de orden, fecha/hora, estado, modalidad de entrega, dirección (si aplica), detalle de productos y total. | Sistema | **Alta** |

### 3.5 Módulo de Panel Administrador

| ID | Descripción | Actor | Prioridad |
|---|---|---|---|
| **RF-AD-01** | El panel mostrará un dashboard con métricas: total de pedidos del día, pedidos pendientes, productos con stock bajo (< 5 unidades) y ventas totales del mes. | Admin | **Alta** |
| **RF-AD-02** | El Administrador podrá gestionar productos: crear, editar atributos, activar/desactivar y ajustar stock manualmente. | Admin | **Alta** |
| **RF-AD-03** | El Administrador podrá gestionar todos los pedidos: ver detalle, cambiar estado y consultar historial completo. | Admin | **Alta** |
| **RF-AD-04** | El Administrador podrá gestionar usuarios: crear Vendedores y Administradores, activar/desactivar cuentas y consultar historial de pedidos por cliente. | Admin | **Alta** |
| **RF-AD-05** | El Administrador podrá generar un reporte de ventas filtrable por rango de fechas, exportable en formato CSV. | Admin | **Media** |
| **RF-AD-06** | El Administrador podrá consultar el historial de pedidos de cualquier comprador registrado. | Admin | **Media** |

---

## 4. Requerimientos No Funcionales

> Clasificados según el modelo de calidad ISO/IEC 25010.

### 4.1 Rendimiento

| ID | Descripción | Prioridad |
|---|---|---|
| **RNF-RD-01** | El tiempo de carga inicial del portal no deberá superar los 3 segundos en conexiones de banda ancha estándar (10 Mbps). | **Alta** |
| **RNF-RD-02** | Las operaciones de consulta al catálogo y detalle de producto deberán responder en menos de 1 segundo. | **Alta** |
| **RNF-RD-03** | El sistema deberá soportar al menos 50 usuarios concurrentes sin degradación perceptible del servicio en el MVP. | **Media** |

### 4.2 Seguridad

| ID | Descripción | Prioridad |
|---|---|---|
| **RNF-SE-01** | Toda la comunicación entre el cliente (browser) y el servidor deberá realizarse mediante HTTPS (TLS 1.2 o superior). | **Alta** |
| **RNF-SE-02** | El sistema implementará autenticación basada en tokens JWT con firma segura y expiración. Los tokens de refresco no deberán exceder 7 días de vigencia. | **Alta** |
| **RNF-SE-03** | El sistema aplicará control de acceso basado en roles (RBAC): ningún usuario podrá acceder a rutas o datos fuera de su rol asignado. | **Alta** |
| **RNF-SE-04** | El sistema protegerá las APIs contra ataques de inyección SQL y XSS mediante validación y sanitización de entradas en el backend. | **Alta** |
| **RNF-SE-05** | El sistema limitará los intentos de generación de OTP a 3 intentos por correo electrónico en un periodo de 10 minutos (rate limiting). | **Alta** |
| **RNF-SE-06** | Los datos personales de los compradores deberán almacenarse cumpliendo la Ley 1581 de 2012 (Habeas Data, Colombia). | **Alta** |
| **RNF-SE-07** | Las contraseñas nunca se almacenarán en texto plano. Se aplicará hashing con BCrypt (costo mínimo 10). | **Alta** |

### 4.3 Usabilidad

| ID | Descripción | Prioridad |
|---|---|---|
| **RNF-US-01** | El portal deberá ser completamente responsive: funcional y visualmente correcto en dispositivos móviles (320px+), tablets y desktop. | **Alta** |
| **RNF-US-02** | El flujo completo de compra (catálogo → carrito → pedido) deberá completarse en no más de 5 pasos o interacciones del usuario. | **Alta** |
| **RNF-US-03** | El sistema mostrará mensajes de error claros y en español ante cualquier operación fallida. | **Alta** |
| **RNF-US-04** | El idioma de toda la interfaz de usuario será español (Colombia). | **Alta** |

### 4.4 Disponibilidad y Confiabilidad

| ID | Descripción | Prioridad |
|---|---|---|
| **RNF-DI-01** | El sistema deberá tener una disponibilidad mínima del 95% mensual en el entorno de producción. | **Alta** |
| **RNF-DI-02** | La base de datos PostgreSQL contará con respaldos (backups) automáticos diarios, con retención mínima de 7 días, gestionados por Railway. | **Alta** |
| **RNF-DI-03** | Ante una falla del servidor, el sistema mostrará una página de error amigable en lugar de un error técnico expuesto. | **Media** |

### 4.5 Mantenibilidad

| ID | Descripción | Prioridad |
|---|---|---|
| **RNF-MA-01** | El código fuente del backend (Spring Boot) seguirá los principios de arquitectura en capas: Controller → Service → Repository. | **Alta** |
| **RNF-MA-02** | El código fuente se versionará en un repositorio Git con ramas diferenciadas para desarrollo, staging y producción. | **Alta** |
| **RNF-MA-03** | Las variables de entorno (credenciales, URLs, claves) no deberán estar hardcodeadas en el código fuente. | **Alta** |

---

## 5. Flujo de Estados del Pedido

| Estado | Ruta Domicilio | Ruta Pick Up |
|---|---|---|
| **1. PENDING** | Pedido recién creado. Esperando confirmación del Vendedor/Admin. | Pedido recién creado. Esperando confirmación del Vendedor/Admin. |
| **2. CONFIRMED** | El negocio acepta el pedido. Inicia preparación. | El negocio acepta el pedido. Inicia preparación. |
| **3. PREPARING** | Se está preparando y empacando el pedido. | Se está preparando y empacando el pedido. |
| **4A. ON_THE_WAY** | El pedido fue entregado al mensajero y está en camino. | — No aplica — |
| **4B. READY_PICKUP** | — No aplica — | El pedido está listo en el punto físico para ser recogido. |
| **5. DELIVERED** | Pedido recibido por el comprador. Estado final exitoso. | Comprador retiró el pedido en el punto físico. Estado final exitoso. |
| **CANCELLED** | Disponible desde PENDING o CONFIRMED. Restituye stock automáticamente. | Disponible desde PENDING o CONFIRMED. Restituye stock automáticamente. |

---

## 6. Stack Tecnológico Definido

| Capa | Tecnología | Justificación |
|---|---|---|
| **Frontend** | Next.js 14+ / React 18 | Framework React con SSR/SSG. Óptimo para SEO y rendimiento. Despliegue nativo en Vercel. |
| **Backend** | Spring Boot 3.x / Java 17+ | Framework Java maduro con ecosistema robusto. Arquitectura en capas. Soporte nativo de JWT y seguridad. |
| **Base de Datos** | PostgreSQL 15+ | Base de datos relacional robusta, open source. Soporte completo en Railway con backups automáticos. |
| **ORM** | Spring Data JPA / Hibernate | ORM estándar del ecosistema Spring. Simplifica el mapeo objeto-relacional. |
| **Autenticación** | JWT + Spring Security | Tokens sin estado. RBAC implementado con Spring Security. |
| **Email** | JavaMail / SendGrid | Envío de notificaciones automáticas en cambios de estado del pedido. |
| **Imágenes** | Cloudinary | Servicio de almacenamiento y optimización de imágenes. Tier gratuito suficiente para el MVP. |
| **Deploy Frontend** | Vercel | Plataforma optimizada para Next.js. Tier gratuito disponible. |
| **Deploy Backend + BD** | Railway | Plataforma PaaS económica (~$5 USD/mes). Soporta Spring Boot y PostgreSQL en el mismo proyecto. |
| **Control de Versiones** | Git / GitHub | Versionado del código con ramas: main (prod), develop, feature/*. |

---

## 7. Matriz de Trazabilidad

| ID RF | Descripción Corta | Módulo | Actor | MVP |
|---|---|---|---|---|
| RF-AU-01 | Registro Comprador con OTP | Autenticación | Comprador | ✅ |
| RF-AU-02 | Captura datos en primer pedido | Autenticación | Comprador | ✅ |
| RF-AU-03 | Login email + contraseña | Autenticación | Comprador | ✅ |
| RF-AU-03B | Recuperación de contraseña | Autenticación | Comprador | ✅ |
| RF-AU-04 | Cierre de sesión | Autenticación | Comprador | ✅ |
| RF-AU-05 | Admin crea Vendedor/Admin | Autenticación | Admin | ✅ |
| RF-AU-06 | Activar/Desactivar usuarios | Autenticación | Admin | ✅ |
| RF-AU-07 | Gestión JWT y RBAC | Autenticación | Sistema | ✅ |
| RF-AU-08 | Editar perfil | Autenticación | Comprador | ⬜ |
| RF-CA-01 | Catálogo con atributos completos | Catálogo | Comprador | ✅ |
| RF-CA-02 | Filtro por género musical | Catálogo | Comprador | ✅ |
| RF-CA-03 | Vista de detalle de producto | Catálogo | Comprador | ✅ |
| RF-CA-04 | Ocultar productos sin stock | Catálogo | Sistema | ✅ |
| RF-CA-05 | Crear producto | Catálogo | Vendedor/Admin | ✅ |
| RF-CA-06 | Editar producto | Catálogo | Vendedor/Admin | ✅ |
| RF-CA-07 | Activar/Desactivar producto | Catálogo | Vendedor/Admin | ✅ |
| RF-CA-08 | Búsqueda por nombre/género | Catálogo | Comprador | ⬜ |
| RF-CR-01 | Agregar productos al carrito | Carrito | Comprador | ✅ |
| RF-CR-02 | Modificar cantidades | Carrito | Comprador | ✅ |
| RF-CR-03 | Eliminar producto del carrito | Carrito | Comprador | ✅ |
| RF-CR-04 | Resumen de compra | Carrito | Comprador | ✅ |
| RF-CR-05 | Validar stock en carrito | Carrito | Sistema | ✅ |
| RF-CR-06 | Elegir modalidad de entrega | Carrito | Comprador | ✅ |
| RF-CR-07 | Confirmar dirección domicilio | Carrito | Comprador | ✅ |
| RF-PE-01 | Crear pedido con estado PENDING | Pedidos | Sistema | ✅ |
| RF-PE-02 | Descontar stock al confirmar | Pedidos | Sistema | ✅ |
| RF-PE-03 | Ciclo de vida del pedido | Pedidos | Sistema | ✅ |
| RF-PE-04 | Notificaciones email por estado | Pedidos | Sistema | ✅ |
| RF-PE-05 | Historial de pedidos del Comprador | Pedidos | Comprador | ✅ |
| RF-PE-06 | Listado de pedidos activos | Pedidos | Vendedor/Admin | ✅ |
| RF-PE-07 | Actualizar estado del pedido | Pedidos | Vendedor/Admin | ✅ |
| RF-PE-08 | Restituir stock al cancelar | Pedidos | Sistema | ✅ |
| RF-PE-09 | Almacenar datos completos del pedido | Pedidos | Sistema | ✅ |
| RF-AD-01 | Dashboard con métricas | Panel Admin | Admin | ✅ |
| RF-AD-02 | Gestión completa de productos | Panel Admin | Admin | ✅ |
| RF-AD-03 | Gestión completa de pedidos | Panel Admin | Admin | ✅ |
| RF-AD-04 | Gestión de usuarios | Panel Admin | Admin | ✅ |
| RF-AD-05 | Reporte de ventas exportable | Panel Admin | Admin | ⬜ |
| RF-AD-06 | Historial de pedidos por cliente | Panel Admin | Admin | ⬜ |

> ✅ Incluido en el MVP   |   ⬜ Fase posterior

---

## 8. Control de Versiones del Documento

| Versión | Fecha | Autor | Descripción del Cambio |
|---|---|---|---|
| v1.0 | Marzo 2026 | Equipo de Desarrollo | Versión inicial del SRS. |
| v1.3 | Marzo 2026 | Equipo de Desarrollo | Corrección en sección 2.3: columna Forma de Acceso del Comprador actualizada a email + contraseña con OTP único de verificación. |

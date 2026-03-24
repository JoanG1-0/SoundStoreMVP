# Plan de Proyecto — Roadmap & Cronograma MVP SoundStore
*Portal de Venta de Memorias USB con Música*

| Campo | Valor |
|---|---|
| **Cliente** | Pikiña |
| **Proyecto** | SoundStore — MVP |
| **Versión del Documento** | v1.0 |
| **Fecha de Inicio** | 7 de Abril de 2026 |
| **Fecha de Cierre** | 2 de Mayo de 2026 |
| **Equipo** | 1 Desarrollador Fullstack |
| **Capacidad Total** | 80 horas (4h/día × 20 días hábiles) |
| **Herramienta de Gestión** | Jira |
| **Metodología** | Ágil — Scrum (sprints semanales) |
| **Estado** | Borrador — Pendiente de Aprobación |

---

## 1. Resumen Ejecutivo

El presente Plan de Proyecto define la planificación detallada para el desarrollo del MVP de SoundStore en un plazo de cuatro (4) semanas calendario, comprendidas entre el 7 de abril y el 2 de mayo de 2026.

El equipo está compuesto por un (1) desarrollador fullstack con dedicación de medio tiempo (4 horas diarias, lunes a viernes), para una capacidad total de 80 horas de desarrollo efectivo. La planificación está organizada en cuatro sprints semanales, cada uno con objetivos claros, tareas estimadas y criterios de aceptación definidos.

La gestión de tareas se realizará en Jira, donde cada ítem del backlog estará asociado a su requerimiento funcional correspondiente (RF-XX-XX) del SRS v1.3.

### 1.1 Capacidad del Equipo

| Parámetro | Valor | Resultado |
|---|---|---|
| Desarrolladores | 1 Fullstack | — |
| Horas/día | 4 horas | — |
| Días laborables/semana | 5 días (L–V) | 20 h/semana |
| Semanas del proyecto | 4 semanas | **80 horas totales** |
| Buffer de contingencia (10%) | ~8 horas reservadas | **72 h efectivas de desarrollo** |

### 1.2 Distribución de Horas por Sprint

| Sprint | Fechas | Enfoque Principal | Horas | Estado |
|---|---|---|---|---|
| **S1** | 7 – 11 Abr 2026 | Setup + Autenticación (registro, login, OTP, JWT, RBAC) | **20 h** | Pendiente |
| **S2** | 14 – 18 Abr 2026 | Catálogo de productos + módulo de carrito de compras | **20 h** | Pendiente |
| **S3** | 22 – 25 Abr 2026 | Módulo de pedidos + notificaciones por email | **16 h** | Pendiente |
| **S4** | 28 Abr – 2 May 2026 | Panel Admin/Vendedor + QA + despliegue en producción | **20 h** | Pendiente |
| **TOTAL** | 7 Abr – 2 May 2026 | MVP completo desplegado en producción | **80 h** | — |

---

## 2. Backlog de Tareas por Sprint

### Sprint 1 — Semana 1 | 7–11 Abril 2026 | Capacidad: 20 h

| ID | Tarea | Área | h | RF(s) |
|---|---|---|---|---|
| **T-01** | Repositorio Git (main, develop, feature/*) + Jira configurado | Setup | 1h | — |
| **T-02** | Proyecto Spring Boot 3.x — estructura en capas (Controller/Service/Repo) | Setup | 1h | RNF-MA-01 |
| **T-03** | Proyecto Next.js 14+ — estructura de carpetas + rutas base | Setup | 1h | — |
| **T-04** | BD PostgreSQL en Railway + variables de entorno (.env) | DevOps | 1h | RNF-MA-03 |
| **T-05** | Conexión Spring Boot ↔ PostgreSQL (JPA/Hibernate + migraciones) | Backend | 1h | — |
| **T-06** | Entidad users + repositorio + seed Admin inicial | Backend | 1h | RF-AU-01, RF-AU-05 |
| **T-07** | Registro Comprador: email + contraseña + BCrypt (costo 10) | Backend | 2h | RF-AU-01, RNF-SE-07 |
| **T-08** | Servicio OTP: generar, enviar por email (SendGrid), validar, expirar | Backend | 2h | RF-AU-01, RF-AU-03B, RNF-SE-05 |
| **T-09** | Login JWT + Spring Security + RBAC (3 roles) | Backend | 3h | RF-AU-03, RF-AU-07, RNF-SE-02, RNF-SE-03 |
| **T-10** | Recuperación de contraseña (OTP → nueva contraseña) | Backend | 1h | RF-AU-03B |
| **T-11** | Página de registro + verificación OTP (formulario + validaciones) | Frontend | 2h | RF-AU-01 |
| **T-12** | Página de login + flujo recuperación de contraseña | Frontend | 2h | RF-AU-03, RF-AU-03B |
| **T-13** | Admin: crear Vendedor/Admin + activar/desactivar usuarios (endpoints) | Backend | 1h | RF-AU-05, RF-AU-06 |

### Sprint 2 — Semana 2 | 14–18 Abril 2026 | Capacidad: 20 h

| ID | Tarea | Área | h | RF(s) |
|---|---|---|---|---|
| **T-14** | Entidad products + CRUD backend (crear, editar, activar/desactivar) | Backend | 3h | RF-CA-05, RF-CA-06, RF-CA-07 |
| **T-15** | Integración Cloudinary para carga de imágenes de productos | Backend | 1h | RF-CA-05 |
| **T-16** | Endpoint catálogo público (filtro género, ocultar sin stock/inactivos) | Backend | 2h | RF-CA-01, RF-CA-02, RF-CA-04 |
| **T-17** | Endpoint búsqueda por nombre/género + detalle de producto | Backend | 1h | RF-CA-03, RF-CA-08 |
| **T-18** | Página catálogo público (grid + filtro por género) | Frontend | 3h | RF-CA-01, RF-CA-02 |
| **T-19** | Página detalle de producto | Frontend | 1h | RF-CA-03 |
| **T-20** | Lógica de carrito: estado global (agregar, modificar cantidad, eliminar) | Frontend | 3h | RF-CR-01, RF-CR-02, RF-CR-03 |
| **T-21** | Página carrito (resumen + validación stock + selección modalidad entrega) | Frontend | 3h | RF-CR-04, RF-CR-05, RF-CR-06, RF-CR-07 |
| **T-22** | Endpoint validación de stock en backend al confirmar carrito | Backend | 1h | RF-CR-05 |
| **T-23** | Perfil Comprador: endpoint editar datos + página frontend | Backend | 1h | RF-AU-08 |
| **T-24** | Ajustes responsivos (móvil/tablet) catálogo y carrito | Frontend | 1h | RNF-US-01 |

### Sprint 3 — Semana 3 | 22–25 Abril 2026 | Capacidad: 16 h

| ID | Tarea | Área | h | RF(s) |
|---|---|---|---|---|
| **T-25** | Entidades orders + order_items + lógica de creación de pedido | Backend | 3h | RF-PE-01, RF-PE-02, RF-PE-09 |
| **T-26** | Máquina de estados del pedido (transiciones permitidas + cancelación) | Backend | 2h | RF-PE-03, RF-PE-08 |
| **T-27** | Servicio notificaciones email en cada cambio de estado del pedido | Backend | 2h | RF-PE-04 |
| **T-28** | Endpoints: listar pedidos activos (Vendedor/Admin) + actualizar estado | Backend | 2h | RF-PE-06, RF-PE-07 |
| **T-29** | Endpoint historial de pedidos del Comprador | Backend | 1h | RF-PE-05 |
| **T-30** | Página confirmación de pedido + número de orden | Frontend | 2h | RF-PE-01 |
| **T-31** | Página historial de pedidos del Comprador (estados + detalle) | Frontend | 2h | RF-PE-05 |
| **T-32** | Endpoints Admin: crear Vendedor/Admin + gestión de usuarios | Backend | 2h | RF-AU-05, RF-AU-06 |

### Sprint 4 — Semana 4 | 28 Abr – 2 May 2026 | Capacidad: 20 h

| ID | Tarea | Área | h | RF(s) |
|---|---|---|---|---|
| **T-33** | Endpoints dashboard Admin: métricas del día + stock bajo + ventas mes | Backend | 2h | RF-AD-01 |
| **T-34** | Endpoint reporte de ventas CSV (filtrable por rango de fechas) | Backend | 2h | RF-AD-05 |
| **T-35** | Panel Admin: dashboard de métricas visuales | Frontend | 2h | RF-AD-01 |
| **T-36** | Panel Admin/Vendedor: gestión de productos (crear, editar, stock) | Frontend | 2h | RF-AD-02 |
| **T-37** | Panel Admin/Vendedor: gestión de pedidos (listado + cambio de estado) | Frontend | 2h | RF-AD-03 |
| **T-38** | Panel Admin: gestión de usuarios (crear, activar/desactivar, historial) | Frontend | 2h | RF-AD-04 |
| **T-39** | Pruebas funcionales E2E: flujo completo registro → compra → pedido | QA | 2h | Todos RF-CR / RF-PE |
| **T-40** | Corrección de bugs detectados en QA | QA | 2h | — |
| **T-41** | Deploy frontend Vercel + deploy backend/BD Railway + HTTPS | DevOps | 2h | RNF-SE-01, RNF-DI-01 |
| **T-42** | Smoke test en producción + entrega formal del MVP al cliente | Docs | 2h | — |

> **Total estimado: 80 horas | 42 tareas en 4 sprints | Buffer de contingencia: ~8 h reservadas en S4**

---

## 3. Hitos del Proyecto y Criterios de Aceptación

| # | Hito | Fecha | Criterio de Aceptación | Estado |
|---|---|---|---|---|
| **H1** | **Kick-off** | 7 Abr 2026 | Project Charter firmado. Repositorio Git creado. Jira configurado con backlog completo. | Pendiente |
| **H2** | **Auth OK** | 11 Abr 2026 | Comprador puede registrarse, verificar email con OTP, iniciar sesión y recuperar contraseña. JWT + RBAC funcional. | Pendiente |
| **H3** | **Catálogo + Carrito** | 18 Abr 2026 | Catálogo muestra productos, filtra por género y permite agregar al carrito. Carrito valida stock y modalidad de entrega. | Pendiente |
| **H4** | **Pedidos + Email** | 25 Abr 2026 | Comprador confirma pedido. Sistema descuenta stock, genera número de orden y envía email automático en cada cambio de estado. | Pendiente |
| **H5** | **Panel Admin** | 30 Abr 2026 | Admin gestiona productos, pedidos y usuarios. Dashboard con métricas del día. Vendedor actualiza estados de pedidos. | Pendiente |
| **H6** | **MVP en Producción** | 2 May 2026 | Sistema desplegado en Vercel + Railway. Flujo completo (registro → compra → pedido → notificación) funciona en producción con HTTPS. | Pendiente |

---

## 4. Riesgos del Plan de Desarrollo

| Riesgo | Probabilidad | Impacto | Plan de Mitigación |
|---|---|---|---|
| Subestimación de tareas técnicas complejas (JWT, OTP, email) | **Alto** | **Alto** | Buffer de 8h en S4. Mover RF-CA-08 y RF-AD-05 a fase posterior si hay presión de tiempo. |
| Problemas de integración Cloudinary / SendGrid / Railway | **Medio** | **Medio** | Reservar T-08 (email) y T-15 (Cloudinary) al inicio de sprint para detectar problemas temprano. |
| Scope creep: solicitudes de cambio fuera del MVP | **Medio** | **Alto** | El SRS v1.3 firmado congela el alcance. Cualquier cambio se documenta y pasa a la fase 2. |
| Indisponibilidad del desarrollador | **Bajo** | **Alto** | Prioridad estricta: auth + catálogo + pedidos antes que panel admin. |
| Demoras en creación de cuentas Cloudinary, SendGrid, Railway | **Bajo** | **Medio** | Crear y verificar cuentas en todos los servicios externos durante el día 1 del proyecto. |

---

## 5. Configuración en Jira

### 5.1 Epics en Jira

| Epic | Tareas incluidas | Horas | Sprint(s) |
|---|---|---|---|
| **EPIC-01: Setup & DevOps** | T-01, T-02, T-03, T-04, T-05, T-41 | 8h | S1 / S4 |
| **EPIC-02: Autenticación** | T-06, T-07, T-08, T-09, T-10, T-11, T-12, T-13, T-32 | 15h | S1 / S3 |
| **EPIC-03: Catálogo** | T-14, T-15, T-16, T-17, T-18, T-19 | 11h | S2 |
| **EPIC-04: Carrito** | T-20, T-21, T-22, T-23, T-24 | 9h | S2 |
| **EPIC-05: Pedidos** | T-25, T-26, T-27, T-28, T-29, T-30, T-31 | 14h | S3 |
| **EPIC-06: Panel Admin / Vendedor** | T-33, T-34, T-35, T-36, T-37, T-38 | 12h | S4 |
| **EPIC-07: QA & Entrega** | T-39, T-40, T-42 | 6h | S4 |

### 5.2 Labels Recomendados en Jira

| Label | Uso |
|---|---|
| **mvp-core** | Tareas imprescindibles para el MVP. No se pueden mover a fases posteriores. |
| **nice-to-have** | Tareas deseables pero diferibles si hay presión de tiempo (RF-CA-08, RF-AD-05). |
| **backend** | Tarea de implementación en Spring Boot / Java. |
| **frontend** | Tarea de implementación en Next.js / React. |
| **blocked** | Tarea bloqueada por dependencia externa. |

---

## 6. Control de Versiones del Documento

| Versión | Fecha | Autor | Descripción del Cambio |
|---|---|---|---|
| v1.0 | Marzo 2026 | Equipo de Desarrollo | Versión inicial del Plan de Proyecto. 42 tareas, 4 sprints. Inicio: 7 Abril 2026. |

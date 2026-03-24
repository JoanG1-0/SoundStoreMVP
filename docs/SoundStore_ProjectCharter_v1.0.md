# Project Charter — SoundStore
*Portal de Venta de Memorias USB con Música*

| Campo | Valor |
|---|---|
| **Cliente** | Pikiña |
| **Ciudad de Operación** | Armenia, Quindío, Colombia |
| **Tipo de Proyecto** | Marketplace de Servicios — Software como Servicio |
| **Metodología** | Ágil — Scrum / Kanban |
| **Modalidad de Desarrollo** | Outsourcing (Equipo Externo) |
| **Fecha de Elaboración** | Marzo 2026 |
| **Versión del Documento** | v1.0 |
| **Estado** | Borrador — Pendiente de Aprobación |

---

## 1. Propósito del Documento

El presente Project Charter establece formalmente la autorización de inicio del proyecto SoundStore. Define el alcance general, los objetivos, los stakeholders involucrados, los entregables esperados y las restricciones conocidas al momento de su elaboración.

Este documento sirve como contrato de entendimiento entre el cliente (Pikiña) y el equipo de desarrollo externo (outsourcing), y debe ser revisado y aprobado por ambas partes antes del inicio de cualquier actividad de desarrollo.

---

## 2. Descripción del Proyecto

### 2.1 Antecedentes

El cliente Pikiña opera un punto de venta físico en la ciudad de Armenia, Quindío, donde comercializa diversos productos, siendo las memorias USB cargadas con música uno de sus productos estrella. El proceso de venta actual es completamente presencial, lo que genera:

- Tiempos de espera elevados para los clientes en el punto físico.
- Limitación en el volumen de ventas diarias por capacidad operativa del local.
- Restricción geográfica al mercado local de Armenia.
- Baja eficiencia en el proceso de atención y despacho.

### 2.2 Descripción de la Solución

SoundStore es un portal web de tipo marketplace orientado a la venta de memorias USB con música, que permitirá al cliente digitalizar y optimizar su canal de ventas. La plataforma habilitará a los compradores para realizar pedidos en línea, elegir entre recibir su producto a domicilio o recogerlo en el punto físico, reduciendo así la carga operativa del local y ampliando el alcance comercial del negocio.

---

## 3. Objetivos del Proyecto

### 3.1 Objetivo General

Desarrollar e implementar un portal web de marketplace (SoundStore) que digitalice el proceso de venta de memorias USB con música del cliente Pikiña, optimizando los tiempos de atención y ampliando el alcance del negocio a nivel nacional dentro del territorio colombiano.

### 3.2 Objetivos Específicos

- Reducir el tiempo promedio de atención presencial en el punto físico mediante la habilitación de pedidos en línea.
- Incrementar el volumen potencial de ventas al eliminar la barrera de presencialidad para realizar una compra.
- Proveer al cliente una herramienta administrativa (panel Admin) para gestionar productos, pedidos y usuarios.
- Habilitar dos modalidades de entrega: domicilio y recogida en punto físico (Pick Up).
- Entregar un MVP funcional en un plazo máximo de un (1) mes calendario.

---

## 4. Alcance del Proyecto

### 4.1 Incluido en el Alcance (In Scope)

- Desarrollo del portal web SoundStore con interfaz para tres tipos de usuario: Comprador, Vendedor y Administrador.
- Módulo de catálogo de productos: listado de memorias USB con música predefinida.
- Módulo de gestión de pedidos: creación, seguimiento y actualización de estado de órdenes.
- Modalidades de entrega: envío a domicilio y recogida en punto físico (Pick Up).
- Panel de administración para gestión de inventario, pedidos y usuarios.
- Registro e inicio de sesión de usuarios (Compradores).
- Despliegue del MVP en un entorno de producción accesible desde internet.
- Operación exclusiva para el territorio colombiano.

### 4.2 Excluido del Alcance (Out of Scope)

- Integración con pasarelas de pago en línea (PSP): el pago se gestionará de forma manual/contra entrega en esta versión.
- Aplicación móvil nativa (iOS / Android).
- Integración con sistemas de logística o transporte de terceros.
- Módulo de personalización de contenido de las memorias USB por parte del comprador.
- Expansión a mercados fuera de Colombia.
- Migración de datos históricos del negocio físico.

---

## 5. Stakeholders del Proyecto

| Stakeholder | Rol | Interés / Expectativa | Nivel de Influencia |
|---|---|---|---|
| Pikiña (Dueño del Negocio) | Sponsor / Cliente | Aumentar ventas y reducir tiempos de atención | **Alto** |
| Equipo de Desarrollo (Outsourcing) | Proveedor Técnico | Entregar el producto en tiempo, alcance y calidad | **Alto** |
| Compradores / Clientes Finales | Usuario Final | Comprar de forma rápida y sencilla desde cualquier lugar | Medio |
| Personal del Punto Físico | Usuario Operativo | Reducir carga operativa en atención presencial | Medio |

---

## 6. Roles del Sistema

| Rol | Responsabilidades dentro del Portal |
|---|---|
| **Comprador** | Explorar el catálogo, realizar pedidos, elegir modalidad de entrega (domicilio o Pick Up) y hacer seguimiento de su orden. |
| **Vendedor** | Gestionar el catálogo de productos (crear, editar, deshabilitar), visualizar y actualizar el estado de los pedidos. |
| **Administrador** | Control total del sistema: gestión de usuarios, roles, productos, pedidos, configuración general de la plataforma y reportes. |

---

## 7. Entregables del Proyecto

| # | Entregable | Tipo | Fase |
|---|---|---|---|
| 1 | Project Charter (este documento) | Documento | Inicio |
| 2 | SRS — Especificación de Requerimientos de Software | Documento | Planificación |
| 3 | Diseño de Base de Datos (ERD) | Diagrama | Diseño |
| 4 | Wireframes / Prototipos UI-UX | Diseño | Diseño |
| 5 | MVP funcional del portal SoundStore (Frontend + Backend) | Software | Desarrollo |
| 6 | Manual de Usuario básico (Comprador, Vendedor, Admin) | Documento | Cierre |
| 7 | Producto desplegado en ambiente de producción | Despliegue | Cierre |

---

## 8. Restricciones y Supuestos

### 8.1 Restricciones

- El MVP debe ser entregado en un plazo máximo de un (1) mes calendario desde la aprobación de este documento.
- No se integrarán pasarelas de pago en línea en esta versión del producto.
- El alcance geográfico del marketplace se limita al territorio colombiano.
- El presupuesto del proyecto está sujeto a los límites acordados con el cliente Pikiña.

### 8.2 Supuestos

- El cliente proporcionará en tiempo y forma la información de productos, precios y catálogo inicial de memorias USB.
- El cliente cuenta con acceso a internet estable en el punto físico para operar el panel de administración.
- El equipo de desarrollo externo contará con disponibilidad completa durante el mes de desarrollo del MVP.
- Los pedidos se gestionarán inicialmente con pago contra entrega o en efectivo en el punto físico.
- El cliente se encargará de la logística de envíos a domicilio con su propio operador o mensajero.

---

## 9. Riesgos Iniciales Identificados

| Riesgo | Probabilidad | Impacto | Mitigación Inicial |
|---|---|---|---|
| Cambios de alcance en medio del desarrollo (scope creep) | **Alto** | **Alto** | Definir y congelar el alcance del MVP antes de iniciar el desarrollo. |
| Demoras en la entrega de información por parte del cliente | **Medio** | **Alto** | Establecer fechas límite de entrega de insumos en el cronograma. |
| Plazo de un mes insuficiente para el alcance definido | **Medio** | **Alto** | Priorizar funcionalidades core en el MVP. Dejar el resto para fases posteriores. |
| Baja adopción del portal por parte de los compradores | **Medio** | **Medio** | Diseño centrado en el usuario (UX simple). Capacitación al cliente. |
| Problemas técnicos en el despliegue en producción | **Bajo** | **Alto** | Incluir ambiente de staging para pruebas previas al despliegue. |

---

## 10. Hitos del Proyecto (Roadmap MVP)

| # | Hito | Semana Estimada | Estado |
|---|---|---|---|
| H1 | Aprobación del Project Charter y firma de inicio | Semana 1 | Pendiente |
| H2 | Entrega de SRS y diseño de Base de Datos aprobados | Semana 1 | Pendiente |
| H3 | Wireframes y diseño UI aprobados por el cliente | Semana 1 – 2 | Pendiente |
| H4 | Sprint 1: Autenticación, catálogo y gestión de productos | Semana 2 | Pendiente |
| H5 | Sprint 2: Módulo de pedidos, modalidades de entrega y panel Admin | Semana 3 | Pendiente |
| H6 | Pruebas funcionales, corrección de defectos y ajustes finales | Semana 4 | Pendiente |
| H7 | Despliegue en producción y entrega formal del MVP | Semana 4 | Pendiente |

---

## 11. Aprobación del Proyecto

Al firmar este documento, las partes manifiestan su acuerdo con el alcance, los objetivos, las restricciones y los entregables aquí descritos, y autorizan formalmente el inicio del proyecto SoundStore.

| Por el Cliente — Pikiña | Por el Equipo de Desarrollo |
|---|---|
| Firma: ___________________________ | Firma: ___________________________ |
| Nombre: _________________________ | Nombre: _________________________ |
| Cargo: __________________________ | Cargo: __________________________ |
| Fecha: ___________________________ | Fecha: ___________________________ |

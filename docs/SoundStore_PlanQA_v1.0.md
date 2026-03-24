# Plan de QA — Estrategia de Pruebas MVP SoundStore
*Portal de Venta de Memorias USB con Música*

| Campo | Valor |
|---|---|
| **Cliente** | Pikiña |
| **Proyecto** | SoundStore — MVP |
| **Versión del Documento** | v1.0 |
| **Referencia SRS** | SoundStore SRS v1.3 |
| **Responsable de QA** | Desarrollador Fullstack |
| **Fecha de Elaboración** | Marzo 2026 |
| **Estado** | Borrador — Pendiente de Aprobación |

---

## 1. Introducción

### 1.1 Propósito

El presente Plan de QA define la estrategia de pruebas para el MVP del sistema SoundStore. Establece qué se prueba, cómo se prueba, con qué herramientas, cuándo se ejecutan las pruebas y cuál es el criterio de aceptación para considerar el MVP aprobado y listo para producción.

### 1.2 Alcance de las Pruebas

**Incluido:**
- Pruebas unitarias: validación de servicios y lógica de negocio en el backend (Spring Boot).
- Pruebas de integración: validación de los endpoints de la API contra una base de datos PostgreSQL real.
- Pruebas manuales funcionales: validación del flujo de valor completo desde la perspectiva del usuario final.

**Excluido del MVP:**
- Pruebas E2E automatizadas (Cypress / Playwright): diferidas a fases posteriores.
- Pruebas de carga o estrés.
- Pruebas de penetración o seguridad avanzada.

### 1.3 Criterio de Aceptación del MVP

| Criterio | Condición de Aprobación |
|---|---|
| **Flujo de valor completo** | El flujo registro → catálogo → carrito → pedido → notificación por email funciona de punta a punta sin errores en producción. |
| **Cobertura de RF Alta** | El 100% de los requerimientos funcionales marcados como Alta en el SRS v1.3 deben pasar sus casos de prueba. |
| **Cero defectos críticos** | No puede haber ningún defecto de severidad Crítica o Alta sin resolver al momento de la entrega formal. |
| **Despliegue funcional** | El sistema debe estar desplegado en Vercel + Railway con HTTPS activo y smoke test aprobado en producción. |

---

## 2. Herramientas de Prueba

| Capa | Herramienta | Tipo de Prueba | Justificación |
|---|---|---|---|
| **Backend** | JUnit 5 | Unitaria | Framework estándar del ecosistema Spring Boot. |
| **Backend** | Mockito | Unitaria | Mocking para aislar la unidad bajo prueba sin BD real. |
| **Backend** | Spring Boot Test + Testcontainers | Integración | Levanta PostgreSQL en Docker para pruebas de integración reales. |
| **API** | Postman | Integración / Manual | Colecciones de requests para probar todos los endpoints. |
| **Backend** | JaCoCo | Cobertura de código | Reportes automáticos de cobertura. Meta mínima: 70% en capa de servicios. |
| **Frontend** | Jest + React Testing Library | Unitaria de componentes | Diferido a fase posterior en el MVP por restricción de tiempo. |

---

## 3. Casos de Prueba por Módulo

### 3.1 Módulo de Autenticación

| ID | Caso de Prueba | Pasos | Resultado Esperado | RF |
|---|---|---|---|---|
| **CP-AU-01** | Registro exitoso de Comprador | POST /api/auth/register con datos válidos. | HTTP 201. Usuario creado con `email_verified=false`. OTP enviado. Contraseña en BCrypt. | RF-AU-01 |
| **CP-AU-02** | Registro con email duplicado | Registrar usuario. Intentar registrar otro con el mismo email. | HTTP 409 Conflict. Mensaje de error claro en español. | RF-AU-01 |
| **CP-AU-03** | Verificación OTP correcta | POST /api/auth/verify-otp con código válido. | HTTP 200. `email_verified=true`. Token JWT retornado. | RF-AU-01 |
| **CP-AU-04** | Verificación OTP expirado | Esperar 6 minutos. Intentar validar el OTP. | HTTP 400. Mensaje: código expirado. Cuenta no activada. | RF-AU-01 |
| **CP-AU-05** | Login exitoso | POST /api/auth/login con credenciales correctas. | HTTP 200. Token JWT válido con rol BUYER. | RF-AU-03 |
| **CP-AU-06** | Login con contraseña incorrecta | POST /api/auth/login con contraseña incorrecta. | HTTP 401. Mensaje de error claro. Sin token. | RF-AU-03 |
| **CP-AU-07** | Acceso a ruta protegida sin token | GET /api/products/admin sin header Authorization. | HTTP 403 Forbidden. El endpoint no retorna datos. | RF-AU-07 |
| **CP-AU-08** | Recuperación de contraseña completa | POST forgot-password → obtener OTP → POST reset-password. | HTTP 200 en cada paso. Login exitoso con nueva contraseña. | RF-AU-03B |
| **CP-AU-09** | Rate limiting de OTP | Solicitar OTP 4 veces en menos de 10 minutos con el mismo email. | A partir del 4to intento: HTTP 429 Too Many Requests. | RNF-SE-05 |
| **CP-AU-10** | Admin crea cuenta de Vendedor | POST /api/admin/users con rol SELLER y contraseña temporal. | HTTP 201. Al primer login, sistema solicita cambio de contraseña. | RF-AU-05 |

### 3.2 Módulo de Catálogo

| ID | Caso de Prueba | Pasos | Resultado Esperado | RF |
|---|---|---|---|---|
| **CP-CA-01** | Listar productos activos con stock | GET /api/products (sin autenticación). | HTTP 200. Solo productos con `is_active=true` y `stock>0`. Atributos completos. | RF-CA-01, RF-CA-04 |
| **CP-CA-02** | Filtrar catálogo por género | GET /api/products?genre=Salsa. | HTTP 200. Solo productos del género indicado. | RF-CA-02 |
| **CP-CA-03** | Ver detalle de un producto | GET /api/products/{id} con ID válido. | HTTP 200. Todos los atributos del producto retornados correctamente. | RF-CA-03 |
| **CP-CA-04** | Producto con stock 0 oculto | Crear producto con `stock=0`. GET /api/products. | El producto NO aparece en el listado público. | RF-CA-04 |
| **CP-CA-05** | Crear producto (Vendedor) | Login como Vendedor. POST /api/products con atributos requeridos. | HTTP 201. Producto creado con `is_active=true`. Visible en catálogo. | RF-CA-05 |
| **CP-CA-06** | Crear producto sin precio | POST /api/products sin campo price. | HTTP 400. Mensaje de validación claro. Producto no creado. | RF-CA-05 |
| **CP-CA-07** | Desactivar producto | PATCH /api/products/{id}/status con `is_active=false`. | HTTP 200. Producto desaparece del catálogo público. Visible en panel Admin. | RF-CA-07 |

### 3.3 Módulo de Carrito

| ID | Caso de Prueba | Pasos | Resultado Esperado | RF |
|---|---|---|---|---|
| **CP-CR-01** | Agregar producto al carrito | POST /api/cart con product_id y quantity=2. | HTTP 200. Carrito actualizado. | RF-CR-01 |
| **CP-CR-02** | Agregar cantidad mayor al stock | Producto con stock=3. POST /api/cart con quantity=5. | HTTP 400. Mensaje: stock insuficiente. Carrito no modificado. | RF-CR-05 |
| **CP-CR-03** | Modificar cantidad en carrito | PUT /api/cart/{itemId} con nueva quantity. | HTTP 200. Total del carrito recalculado correctamente. | RF-CR-02 |
| **CP-CR-04** | Eliminar producto del carrito | DELETE /api/cart/{itemId}. | HTTP 200. Solo el ítem indicado eliminado. | RF-CR-03 |
| **CP-CR-05** | Ver resumen del carrito | GET /api/cart con productos en carrito. | HTTP 200. Nombre, cantidad, precio unitario y totales correctos. | RF-CR-04 |
| **CP-CR-06** | Seleccionar modalidad Domicilio | PUT /api/cart/delivery con `delivery_type=DELIVERY` y dirección válida. | HTTP 200. Modalidad y dirección guardadas. | RF-CR-06, RF-CR-07 |
| **CP-CR-07** | Seleccionar modalidad Pick Up | PUT /api/cart/delivery con `delivery_type=PICKUP`. | HTTP 200. `delivery_address` nulo. Carrito listo. | RF-CR-06 |

### 3.4 Módulo de Pedidos

| ID | Caso de Prueba | Pasos | Resultado Esperado | RF |
|---|---|---|---|---|
| **CP-PE-01** | Confirmar pedido exitosamente | POST /api/orders/confirm. | HTTP 201. Pedido con estado `PENDING`. Número de orden generado (`SS-XXXXXXXX`). Stock descontado. | RF-PE-01, RF-PE-02 |
| **CP-PE-02** | Verificar snapshot de precio | Crear pedido. Cambiar precio del producto. Consultar pedido. | El pedido histórico conserva el precio original. | RF-PE-09 |
| **CP-PE-03** | Notificación email al crear pedido | Confirmar pedido como Comprador. | Email recibido con número de orden y estado `PENDING`. | RF-PE-04 |
| **CP-PE-04** | Cambio de estado PENDING → CONFIRMED | PATCH /api/orders/{id}/status con `status=CONFIRMED`. | HTTP 200. Estado actualizado. Email automático enviado al Comprador. | RF-PE-07, RF-PE-04 |
| **CP-PE-05** | Transición de estado inválida | Pedido en `PREPARING`. Intentar cambiar a `PENDING`. | HTTP 400. Transición no permitida. Estado no modificado. | RF-PE-03 |
| **CP-PE-06** | Cancelar pedido desde PENDING | PATCH /api/orders/{id}/status con `status=CANCELLED`. | HTTP 200. Pedido cancelado. Stock restituido. Email de cancelación enviado. | RF-PE-03, RF-PE-08 |
| **CP-PE-07** | Cancelar pedido desde PREPARING (no permitido) | Pedido en `PREPARING`. Intentar cancelar. | HTTP 400. No se puede cancelar desde PREPARING. | RF-PE-03 |
| **CP-PE-08** | Historial de pedidos del Comprador | GET /api/orders/my-orders. | HTTP 200. Lista de todos los pedidos con estado actual y detalle. | RF-PE-05 |
| **CP-PE-09** | Flujo Pick Up completo | Pedido `PICKUP`. Avanzar hasta `READY_PICKUP` → `DELIVERED`. | Flujo correcto sin `delivery_address`. Estado final `DELIVERED`. | RF-PE-03 |
| **CP-PE-10** | Flujo Domicilio completo | Pedido `DELIVERY`. Avanzar hasta `ON_THE_WAY` → `DELIVERED`. | Flujo correcto con `delivery_address` guardado. Estado final `DELIVERED`. | RF-PE-03 |

### 3.5 Módulo de Panel Administrador

| ID | Caso de Prueba | Pasos | Resultado Esperado | RF |
|---|---|---|---|---|
| **CP-AD-01** | Dashboard muestra métricas correctas | GET /api/admin/dashboard. | HTTP 200. Pedidos del día, pendientes, stock bajo y ventas del mes correctos. | RF-AD-01 |
| **CP-AD-02** | Ajuste manual de stock | PATCH /api/products/{id}/stock con nueva cantidad. | HTTP 200. Stock actualizado. Si stock>0 y activo, aparece en catálogo. | RF-AD-02 |
| **CP-AD-03** | Ver historial de pedidos de un cliente | GET /api/admin/users/{userId}/orders. | HTTP 200. Todos los pedidos del Comprador con detalle completo. | RF-AD-06 |
| **CP-AD-04** | Desactivar cuenta de usuario | PATCH /api/admin/users/{id}/status con `is_active=false`. | HTTP 200. Al intentar login: HTTP 403. | RF-AU-06 |
| **CP-AD-05** | Comprador no accede a rutas de Admin | GET /api/admin/dashboard con token de Comprador. | HTTP 403 Forbidden. RBAC correcto. | RNF-SE-03 |

---

## 4. Prueba del Flujo de Valor Completo

> Esta prueba manual es el **criterio principal de aceptación del MVP**. Debe ejecutarse en el entorno de producción antes de la entrega formal. Si algún paso falla, el MVP no puede ser entregado.

### 4.1 Precondiciones

- Sistema desplegado en Vercel + Railway con HTTPS activo.
- Al menos 3 productos cargados en el catálogo con stock suficiente.
- Cuenta de Admin configurada (seed inicial).
- Servicio de email (SendGrid) activo y verificado.

### 4.2 Pasos del Flujo Completo

| # | Acción | Resultado Esperado | Estado |
|---|---|---|---|
| **1** | Acceder al portal desde un celular (responsive). | Carga en menos de 3 segundos. Diseño responsivo sin elementos cortados. | ⬜ Pendiente |
| **2** | Registrar un nuevo Comprador con email y contraseña. | Formulario acepta los datos. Email con OTP enviado. | ⬜ Pendiente |
| **3** | Verificar el email con el código OTP recibido. | Cuenta activada. Comprador redirigido al catálogo o login. | ⬜ Pendiente |
| **4** | Iniciar sesión con email y contraseña. | Login exitoso. Token JWT almacenado. Redirigido al catálogo. | ⬜ Pendiente |
| **5** | Navegar el catálogo y filtrar por género musical. | Solo productos del género seleccionado visibles. Filtro funcional. | ⬜ Pendiente |
| **6** | Ver el detalle de un producto. | Todos los atributos visibles: nombre, descripción, precio, imagen, stock, género. | ⬜ Pendiente |
| **7** | Agregar 2 productos distintos al carrito. | Carrito actualizado. Total calculado correctamente. | ⬜ Pendiente |
| **8** | Ver el resumen del carrito. | Nombre, cantidad, precio unitario y total de cada ítem correctos. | ⬜ Pendiente |
| **9** | Seleccionar modalidad Domicilio y confirmar dirección. | Dirección registrada. Carrito listo para confirmar. | ⬜ Pendiente |
| **10** | Confirmar el pedido. | Pedido creado con estado `PENDING`. Número de orden generado. Email recibido. | ⬜ Pendiente |
| **11** | Login como Vendedor. Cambiar estado a CONFIRMED. | Estado actualizado. Email automático al Comprador. | ⬜ Pendiente |
| **12** | Cambiar estado a EN PREPARACIÓN. | Estado actualizado. Email enviado al Comprador. | ⬜ Pendiente |
| **13** | Cambiar estado a EN CAMINO. | Estado actualizado. Email enviado al Comprador. | ⬜ Pendiente |
| **14** | Cambiar estado a ENTREGADO. | Estado final `DELIVERED`. Email de entrega enviado. | ⬜ Pendiente |
| **15** | Comprador consulta historial de pedidos. | Pedido visible con todos los estados y detalle completo. | ⬜ Pendiente |
| **16** | Login como Admin. Verificar métricas del dashboard. | El pedido refleja en las ventas del día/mes. Métricas correctas. | ⬜ Pendiente |

---

## 5. Gestión de Defectos

### 5.1 Clasificación de Severidad

| Severidad | Definición | Ejemplos |
|---|---|---|
| **Crítica** | El sistema no puede funcionar. Bloquea el flujo de valor completo. | No se puede crear un pedido. Login no funciona. Sistema caído en producción. |
| **Alta** | Funcionalidad principal afectada pero el sistema sigue operando parcialmente. | El stock no se descuenta al confirmar un pedido. Email no se envía. |
| **Media** | Funcionalidad secundaria afectada. Existe un workaround. | El filtro de géneros no funciona. El historial no ordena por fecha. |
| **Baja** | Problema cosmético o de UX menor. No afecta la operación. | Texto mal alineado. Color incorrecto en un botón. Typo en un mensaje. |

### 5.2 Proceso de Gestión en Jira

- Todo defecto se registra como **Bug** en Jira con: título descriptivo, pasos para reproducir, resultado actual vs esperado, severidad y evidencia (screenshot o log).
- Los defectos **Críticos y Altos** deben resolverse antes de la entrega formal del MVP.
- Los defectos **Medios y Bajos** pueden documentarse para corrección en fase posterior.
- Ciclo de vida: `Abierto → En corrección → En verificación → Cerrado`.

---

## 6. Cronograma de Pruebas

| Sprint | Fechas | Pruebas a ejecutar | Criterio de salida del sprint |
|---|---|---|---|
| **S1** | 7–11 Abr 2026 | Unitarias + integración del módulo de Autenticación. Colección Postman de endpoints auth. | CP-AU-01 al CP-AU-10 aprobados. |
| **S2** | 14–18 Abr 2026 | Unitarias + integración de Catálogo y Carrito. Colección Postman ampliada. | CP-CA-01 al CP-CA-07 y CP-CR-01 al CP-CR-07 aprobados. |
| **S3** | 22–25 Abr 2026 | Unitarias + integración del módulo de Pedidos. Verificación de emails automáticos. | CP-PE-01 al CP-PE-10 aprobados. |
| **S4** | 28 Abr–2 May 2026 | Panel Admin. Flujo de valor completo (sección 4). Smoke test en producción. | CP-AD-01 al CP-AD-05 aprobados. Los 16 pasos del flujo completo aprobados en producción. |

---

## 7. Definición de Done (DoD)

Una funcionalidad se considera **DONE** únicamente cuando cumple **todos** los criterios siguientes:

| # | Criterio de Done |
|---|---|
| **1** | El código fue escrito, revisado y empujado a la rama `develop` en GitHub. |
| **2** | Las pruebas unitarias correspondientes fueron escritas y pasan en verde. |
| **3** | Los casos de prueba de integración del módulo pasan correctamente con Testcontainers. |
| **4** | Los endpoints del módulo fueron probados manualmente con Postman y los resultados son los esperados. |
| **5** | No existen defectos de severidad Crítica o Alta abiertos relacionados con el módulo. |
| **6** | El reporte de cobertura JaCoCo muestra al menos 70% en la capa de servicios del módulo. |
| **7** | El módulo fue probado en dispositivo móvil y desktop (responsive verificado). |
| **8** | Los mensajes de error están en español y son claros para el usuario final. |

---

## 8. Control de Versiones del Documento

| Versión | Fecha | Autor | Descripción del Cambio |
|---|---|---|---|
| v1.0 | Marzo 2026 | Equipo de Desarrollo | Versión inicial del Plan de QA. 42 casos de prueba distribuidos en 5 módulos. Flujo de valor completo con 16 pasos. |

# Plan de Despliegue — Deployment Plan MVP SoundStore
*Portal de Venta de Memorias USB con Música*

| Campo | Valor |
|---|---|
| **Cliente** | Pikiña |
| **Proyecto** | SoundStore — MVP |
| **Versión del Documento** | v1.0 |
| **Referencia SRS** | SoundStore SRS v1.3 |
| **Responsable de Despliegue** | Desarrollador Fullstack |
| **Plataformas** | Vercel (Frontend) + Railway (Backend + BD) |
| **CI/CD** | GitHub Actions |
| **Fecha de Elaboración** | Marzo 2026 |
| **Estado** | Borrador — Pendiente de Aprobación |

---

## 1. Introducción

### 1.1 Propósito

El presente Plan de Despliegue define la estrategia, los ambientes, las herramientas y los procedimientos necesarios para llevar el sistema SoundStore desde el entorno de desarrollo hasta el entorno de producción de forma controlada, repetible y segura.

### 1.2 Alcance

- Definición y configuración de los tres ambientes: desarrollo local, staging y producción.
- Estrategia de CI/CD mediante GitHub Actions para automatizar el flujo de integración y despliegue.
- Variables de entorno y gestión de secretos por ambiente.
- Proceso de despliegue paso a paso para frontend (Vercel) y backend + BD (Railway).
- Plan de rollback formal ante fallas en producción.
- Checklist de smoke test post-despliegue.

### 1.3 Restricciones

- El dominio del portal será el subdominio gratuito de Vercel (`soundstore.vercel.app`) durante el MVP.
- Solo el desarrollador fullstack tiene acceso a las cuentas de Vercel y Railway.
- No se incluye infraestructura de alta disponibilidad (load balancers, réplicas) en el MVP.

---

## 2. Ambientes del Sistema

| Ambiente | Propósito | Frontend | Backend + BD |
|---|---|---|---|
| **Local (DEV)** | Desarrollo activo. El desarrollador trabaja aquí día a día. | `localhost:3000` (Next.js dev server) | `localhost:8080` (Spring Boot) + PostgreSQL local o Docker |
| **Staging (QA)** | Validación previa al despliegue en producción. Espejo de producción con datos de prueba. | `soundstore-staging.vercel.app` (rama: develop) | Railway — proyecto staging (BD separada de producción) |
| **Producción (PROD)** | Ambiente real accesible por el cliente Pikiña y los compradores finales. | `soundstore.vercel.app` (rama: main) | Railway — proyecto producción (BD con backups automáticos diarios) |

### 2.1 Estrategia de Ramas Git por Ambiente

| Rama Git | Ambiente | Descripción |
|---|---|---|
| **feature/*** | Local | Ramas de trabajo por tarea. Se fusionan a `develop` al terminar. |
| **develop** | Staging | Rama de integración. Dispara deploy automático a staging. |
| **main** | Producción | Rama protegida. Solo recibe merges desde `develop` tras aprobación. Dispara deploy automático a producción. |

---

## 3. Variables de Entorno

> ⚠️ Todas las credenciales se gestionan mediante variables de entorno. **Nunca se hardcodean en el código fuente** (RNF-MA-03). Cada ambiente tiene su propio conjunto de variables.

### 3.1 Variables del Backend (Spring Boot — Railway)

| Variable | Ambiente | Descripción |
|---|---|---|
| **DATABASE_URL** | Todos | URL de conexión a PostgreSQL. Generada automáticamente por Railway. |
| **DATABASE_USERNAME** | Todos | Usuario de la base de datos PostgreSQL. |
| **DATABASE_PASSWORD** | Todos | Contraseña de la base de datos. Valor distinto por ambiente. |
| **JWT_SECRET** | Todos | Clave secreta para firma de tokens JWT. Mínimo 256 bits. Valor distinto por ambiente. |
| **JWT_EXPIRATION_MS** | Todos | Tiempo de expiración del token de acceso en milisegundos. Ej: `3600000` (1 hora). |
| **JWT_REFRESH_EXPIRATION_MS** | Todos | Tiempo de expiración del refresh token. Máximo `604800000` (7 días). |
| **SENDGRID_API_KEY** | Staging/Prod | API Key de SendGrid para envío de emails OTP y notificaciones de pedidos. |
| **SENDGRID_FROM_EMAIL** | Staging/Prod | Dirección remitente verificada en SendGrid. Ej: `noreply@soundstore.co` |
| **CLOUDINARY_CLOUD_NAME** | Todos | Nombre del cloud en Cloudinary para almacenamiento de imágenes. |
| **CLOUDINARY_API_KEY** | Todos | API Key de Cloudinary. |
| **CLOUDINARY_API_SECRET** | Todos | API Secret de Cloudinary. Nunca exponer en logs ni en el frontend. |
| **FRONTEND_URL** | Todos | URL del frontend para configuración de CORS. Ej: `https://soundstore.vercel.app` |
| **SPRING_PROFILES_ACTIVE** | Todos | Perfil activo de Spring. Valores: `dev` / `staging` / `prod`. |

### 3.2 Variables del Frontend (Next.js — Vercel)

| Variable | Ambiente | Descripción |
|---|---|---|
| **NEXT_PUBLIC_API_URL** | Todos | URL base del backend. Ej: `https://soundstore-api.railway.app` |
| **NEXT_PUBLIC_APP_NAME** | Todos | Nombre de la aplicación para mostrar en la UI. Ej: `SoundStore` |
| **NEXT_PUBLIC_ENV** | Todos | Ambiente activo. Valores: `development` / `staging` / `production`. |

> ⚠️ Las variables prefijadas con `NEXT_PUBLIC_` son visibles en el navegador del cliente. **Nunca deben contener claves secretas.**

---

## 4. Pipeline CI/CD con GitHub Actions

### 4.1 Pipeline de Staging (rama: `develop`)

Se activa automáticamente con cada push o merge a la rama `develop`.

| # | Paso | Detalle |
|---|---|---|
| **1** | Checkout del código | Clona la rama `develop` en el runner de GitHub Actions. |
| **2** | Setup Java 17 | Configura el JDK 17 en el runner. |
| **3** | Compilar backend | Ejecuta `./gradlew build`. Falla si hay errores de compilación. |
| **4** | Ejecutar pruebas unitarias | Ejecuta `./gradlew test`. Falla si alguna prueba no pasa. |
| **5** | Generar reporte JaCoCo | Falla si la cobertura de la capa de servicios está por debajo del 70%. |
| **6** | Ejecutar pruebas de integración | Levanta Testcontainers (PostgreSQL en Docker) y ejecuta las pruebas de integración. |
| **7** | Build del frontend | Ejecuta `npm run build` en el proyecto Next.js. Falla si hay errores de TypeScript. |
| **8** | Deploy a Staging en Railway | Despliega el backend al proyecto Railway de staging. Usa el secreto `RAILWAY_TOKEN_STAGING`. |
| **9** | Deploy a Staging en Vercel | Despliega el frontend al entorno de preview en Vercel. |
| **10** | Notificación de resultado | Notifica en el PR si el pipeline pasó o falló. |

### 4.2 Pipeline de Producción (rama: `main`)

Se activa únicamente cuando se hace un merge a la rama `main`. La rama `main` está protegida: no se permiten pushes directos.

| # | Paso | Detalle |
|---|---|---|
| **1** | Checkout del código | Clona la rama `main`. |
| **2** | Setup Java 17 | Configura el JDK 17. |
| **3** | Compilar + tests completos | Repite compilación, pruebas unitarias, cobertura JaCoCo y pruebas de integración. |
| **4** | Crear tag de versión | Etiqueta automáticamente el commit con la versión del release (ej: `v1.0.0`). |
| **5** | Deploy backend a Railway PROD | Despliega el JAR al proyecto Railway de producción. Usa el secreto `RAILWAY_TOKEN_PROD`. |
| **6** | Ejecutar migraciones de BD | Railway ejecuta automáticamente las migraciones de Hibernate/Flyway al arrancar. |
| **7** | Deploy frontend a Vercel PROD | Despliega el build de Next.js a `soundstore.vercel.app`. Usa el secreto `VERCEL_TOKEN`. |
| **8** | Smoke test automatizado | Script `curl` valida los endpoints críticos: `GET /api/health`, `GET /api/products`. |
| **9** | Notificación de resultado | Notifica al desarrollador el resultado con la URL de producción y número de versión. |

### 4.3 Secretos de GitHub Actions requeridos

| Nombre del Secreto | Descripción |
|---|---|
| **RAILWAY_TOKEN_STAGING** | Token de API de Railway para el proyecto de staging. |
| **RAILWAY_TOKEN_PROD** | Token de API de Railway para el proyecto de producción. |
| **VERCEL_TOKEN** | Token de API de Vercel para disparar deployments programáticos. |
| **VERCEL_ORG_ID** | ID de la organización en Vercel. |
| **VERCEL_PROJECT_ID** | ID del proyecto en Vercel. |
| **JWT_SECRET_PROD** | Clave secreta de JWT para producción. Mínimo 256 bits, generada aleatoriamente. |
| **SENDGRID_API_KEY_PROD** | API Key de SendGrid para producción. |
| **CLOUDINARY_API_SECRET_PROD** | API Secret de Cloudinary para producción. |

---

## 5. Proceso de Despliegue Manual (Primer Despliegue)

### 5.1 Configuración Inicial — Railway (Backend + BD)

1. Crear cuenta en [railway.app](https://railway.app) con el email del desarrollador.
2. Crear un nuevo proyecto llamado `soundstore-prod`.
3. Agregar un servicio PostgreSQL al proyecto. Railway provisiona la BD y genera la `DATABASE_URL`.
4. Agregar un servicio de tipo 'GitHub Repo' apuntando al repositorio del backend (rama: `main`).
5. Configurar todas las variables de entorno del backend en la sección Variables del servicio.
6. Verificar que Railway ejecuta el build de Gradle/Maven automáticamente.
7. Copiar la URL pública del servicio backend — se usará como `NEXT_PUBLIC_API_URL`.
8. Repetir pasos 2–6 para el proyecto `soundstore-staging` apuntando a la rama `develop`.

### 5.2 Configuración Inicial — Vercel (Frontend)

1. Crear cuenta en [vercel.com](https://vercel.com).
2. Importar el repositorio del frontend desde GitHub.
3. Configurar el framework como Next.js. Vercel lo detecta automáticamente.
4. Configurar las variables de entorno del frontend diferenciando por ambiente (Production / Preview / Development).
5. Vercel asigna automáticamente el dominio `soundstore.vercel.app` al ambiente de producción.
6. Verificar que el deploy inicial funciona correctamente.
7. Habilitar la integración con GitHub para detección automática de pushes.

### 5.3 Configuración Inicial — GitHub Actions

1. En el repositorio de GitHub, ir a `Settings → Secrets and variables → Actions`.
2. Agregar todos los secretos listados en la sección 4.3.
3. Crear el archivo `.github/workflows/staging.yml` con el pipeline de staging.
4. Crear el archivo `.github/workflows/production.yml` con el pipeline de producción.
5. Proteger la rama `main`: `Settings → Branches → Add rule → Require pull request before merging`.
6. Hacer un push de prueba a `develop` para verificar que el pipeline de staging se activa.

---

## 6. Plan de Rollback

### 6.1 Criterios para Activar el Rollback

| Condición | Acción recomendada |
|---|---|
| El flujo de compra (carrito → pedido) no funciona en producción. | **Rollback inmediato. Prioridad máxima.** |
| El login o registro de usuarios falla en producción. | **Rollback inmediato. Prioridad máxima.** |
| El backend responde con errores 500 en más del 20% de las peticiones. | **Rollback inmediato tras confirmación.** |
| Las notificaciones por email dejan de enviarse. | Evaluar corrección en caliente. Rollback solo si no hay fix rápido. |
| Bug visual o de UX sin impacto en funcionalidad core. | No activa rollback. Se corrige en el siguiente deploy. |

### 6.2 Procedimiento de Rollback — Frontend (Vercel)

1. Acceder a `vercel.com` con las credenciales del desarrollador.
2. Navegar al proyecto `soundstore → pestaña Deployments`.
3. Identificar el último deployment estable (el anterior al problemático).
4. Hacer clic en los tres puntos `(...)` del deployment estable → seleccionar **Promote to Production**.
5. Vercel revierte el frontend en menos de 30 segundos sin downtime.
6. Verificar el flujo de valor completo en producción tras el rollback.
7. Registrar el incidente en Jira con: fecha, causa, deployment afectado, deployment de rollback y tiempo de resolución.

### 6.3 Procedimiento de Rollback — Backend (Railway)

1. Acceder a `railway.app` con las credenciales del desarrollador.
2. Navegar al proyecto `soundstore-prod → servicio backend → pestaña Deployments`.
3. Identificar el último deployment estable y hacer clic en **Redeploy**.
4. Railway reinicia el backend con la versión anterior en aproximadamente 60–90 segundos.
5. Si el problema es de base de datos (migración fallida), evaluar restaurar el backup más reciente desde `Railway → pestaña Backups`.
   > ⚠️ **ADVERTENCIA**: restaurar un backup implica pérdida de datos desde el momento del backup hasta ahora.
6. Verificar el estado del backend: `GET /api/health` desde Postman o el navegador.
7. Registrar el incidente en Jira.

### 6.4 Tiempo Objetivo de Recuperación (RTO)

| Componente | RTO Objetivo | Mecanismo |
|---|---|---|
| Frontend (Vercel) | **< 2 minutos** | Promote to Production en la consola de Vercel. |
| Backend (Railway) | **< 5 minutos** | Redeploy del deployment anterior en Railway. |
| Base de Datos (Railway PostgreSQL) | **< 30 minutos** | Restauración del último backup diario automático de Railway. |

---

## 7. Checklist de Smoke Test Post-Despliegue

> Ejecutar después de cada despliegue en producción. Si algún punto falla, se activa el plan de rollback.

| # | Verificación | Resultado |
|---|---|---|
| **1** | El portal carga en el navegador en menos de 3 segundos (`soundstore.vercel.app`). | ⬜ Pendiente |
| **2** | El endpoint de salud del backend responde: `GET /api/health → HTTP 200`. | ⬜ Pendiente |
| **3** | El catálogo público carga y muestra productos: `GET /api/products → HTTP 200`. | ⬜ Pendiente |
| **4** | El registro de un nuevo Comprador funciona y envía el email OTP correctamente. | ⬜ Pendiente |
| **5** | El login con email y contraseña genera un token JWT válido. | ⬜ Pendiente |
| **6** | Es posible agregar un producto al carrito desde el catálogo. | ⬜ Pendiente |
| **7** | Es posible confirmar un pedido y se genera el número de orden (`SS-XXXXXXXX`). | ⬜ Pendiente |
| **8** | El email de confirmación del pedido llega al correo del Comprador. | ⬜ Pendiente |
| **9** | El login como Vendedor funciona y puede ver el listado de pedidos. | ⬜ Pendiente |
| **10** | El login como Admin funciona y el dashboard muestra métricas. | ⬜ Pendiente |
| **11** | HTTPS está activo: el navegador muestra el candado en `soundstore.vercel.app`. | ⬜ Pendiente |
| **12** | No hay errores en la consola del navegador (F12 → Console) en el flujo principal. | ⬜ Pendiente |

---

## 8. Monitoreo Básico en Producción

| Qué monitorear | Herramienta | Cómo acceder |
|---|---|---|
| Disponibilidad del frontend | Vercel Analytics | `vercel.com → proyecto → Analytics` |
| Logs del backend | Railway Logs (tiempo real) | `railway.app → proyecto → servicio → Logs` |
| Uso de recursos del backend | Railway Metrics (CPU, RAM, red) | `railway.app → proyecto → servicio → Metrics` |
| Estado de la base de datos | Railway PostgreSQL Dashboard | `railway.app → proyecto → PostgreSQL → Data` |
| Errores del frontend | Vercel Deployment Logs | `vercel.com → proyecto → Deployments → logs` |
| Backups de la BD | Railway Backup Schedule | `railway.app → PostgreSQL → Backups`. Verificar diariamente. |

### 8.1 Alertas Básicas Recomendadas

- Configurar Railway para enviar notificaciones por email al desarrollador cuando el servicio backend se reinicie inesperadamente.
- Revisar los logs de Railway al menos una vez al día durante la primera semana de producción.
- Verificar el dashboard de Vercel Analytics semanalmente para detectar caídas en el tráfico o errores de carga.

---

## 9. Control de Versiones del Documento

| Versión | Fecha | Autor | Descripción del Cambio |
|---|---|---|---|
| v1.0 | Marzo 2026 | Equipo de Desarrollo | Versión inicial del Plan de Despliegue. 3 ambientes, CI/CD con GitHub Actions, rollback formal y smoke test de 12 puntos. |

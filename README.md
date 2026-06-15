# Proyecto StreamFlow - Arquitectura de Microservicios

> **Integrantes:**
- Vicente Castro
- Claudio Figueroa

---
> **Descripción del Proyecto:**
StreamFlow es un sistema diseñado para la gestión y administración de una plataforma de streaming. Esta versión evoluciona a una **Arquitectura de Microservicios**, integrando comunicación distribuida, documentación técnica, pruebas unitarias y despliegue en la nube mediante contenedores.

---
> **Microservicios Implementados:**
El ecosistema consta de un enrutador central y 10 microservicios de dominio funcionalmente aislados:
1. **API Gateway:** Punto de entrada único, balanceo y enrutador de peticiones hacia los servicios internos.
2. **Pelicula Microservicio:** Gestión del catálogo y metadatos de películas.
3. **Genero Microservicio:** Administración de categorías y géneros cinematográficos.
4. **Valoracion Microservicio:** Registro de calificaciones y reseñas de los usuarios.
5. **Usuario Microservicio:** Gestión de cuentas y perfiles de usuario.
6. **Search Microservicio:** Motor de búsqueda indexada para el ecosistema.
7. **Subscription Microservicio:** Control de planes, suscripciones y facturación.
8. **Watch History Microservicio:** Registro y seguimiento del historial de visualización.
9. **Favorites Microservicio:** Administración de listas de reproducción favoritas por usuario.
10. **Notification Microservicio:** Sistema de envío de alertas y comunicaciones.

---
> **Tecnologías y Patrones de Arquitectura:**
- **Framework Core:** Java 21 y Spring Boot 3.3.1
- **Comunicación Interna (Síncrona):** Spring Cloud OpenFeign
- **Enrutamiento:** Spring Cloud Gateway
- **Persistencia de Datos:** Spring Data JPA con bases de datos relacionales (PostgreSQL para Producción / MySQL para Desarrollo).
- **Documentación de APIs:** Springdoc OpenAPI (Swagger UI v2.6.0)
- **Testing:** JUnit & Mockito (Cobertura de Pruebas Unitarias)
- **Infraestructura y Contenedores:** Docker (Multi-stage builds en cada microservicio)
- **Despliegue e Integración:** Nube PaaS (Render).

---
> **Configuración de Entornos (Profiles):**
El proyecto implementa el patrón de desacoplamiento de configuración aislando los entornos mediante Spring Profiles:
- `application.yml`: Archivo orquestador base.
- `application-dev.yml`: Entorno local (Credenciales fijas, base de datos local).
- `application-prod.yml`: Entorno de producción en la nube (Inyección de Variables de Entorno dinámica).

---
> **Instrucciones de Ejecución (Despliegue Local):**
1. Clonar el repositorio.
2. Configurar una base de datos local (MySQL/PostgreSQL) para cada microservicio.
3. En el archivo `application.yml` de cada microservicio, asegurar que el perfil activo sea el de desarrollo:
   ```yaml
   spring:
     profiles:
       active: dev
4. Levantar cada microservicio individualmente desde el IDE o vía Maven.
5. Levantar el API Gateway.
6. Consumir los endpoints a través del puerto expuesto por el Gateway. La base de datos creará las tablas automáticamente y el archivo data.sql inyectará la data semilla.

> **Instrucciones de Despliegue (Render):**

1. Cada microservicio cuenta con su propio Dockerfile. Al desplegar, se debe apuntar el Root Directory a la subcarpeta correspondiente (ej. /pelicula-microservicio).
2. En la plataforma Render, inyectar obligatoriamente las siguientes Variables de Entorno:
- **SPRING_PROFILES_ACTIVE** = prod (Obliga a leer el YML de producción).
- **DB_URL** = `jdbc:postgresql://dpg-d8m4sus8aovs73eacilg-a:5432/streamflow_db_bqcn`
- **DB_USER** = `admin`
- **DB_PASS** = `1TZFfNVNicBOOiuAOczPxTXQd4aQ91uq`
- Variables de Interoperabilidad (Feign): PELICULA_SERVICE_URL, GENERO_SERVICE_URL, etc., apuntando a las URLs internas/públicas que entregue el proveedor Cloud.

> **Documentación Técnica (Swagger UI):**

La documentación interactiva de cada módulo es generada dinámicamente y se encuentra disponible agregando la siguiente ruta al dominio del microservicio (ya sea en localhost o en la URL en la nube):
`.../swagger-ui/index.html`
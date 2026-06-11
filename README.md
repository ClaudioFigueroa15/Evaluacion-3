# Proyecto StreamFlow

> **Integrantes:**
- Vicente Castro 
- Claudio Figueroa
---
> **Descripción del Proyecto:**
- Sistema de gestión estructurado bajo el patrón CSR para administrar películas, géneros y valoraciones hechas por usuarios. El sistema garantiza la persistencia relacional con JPA y aplica validaciones robustas (JSR 380).
---
> Funcionalidades Implementadas:
- Listado de películas, géneros y valoraciones.
- Creación e Inserción de películas, géneros y valoraciones en base de datos.
- Obtención de datos por ID (película, género, valoración).
- Obtención de películas filtradas por género o año de estreno.
- Obtención de valoración especificación de película.
- Actualización o Modificación de datos ya existentes en la base de datos. 
- Eliminación de datos de la base de datos a partir de Id.
---
> **Pasos de Ejecución:**
1. Abrir Laragon e Iniciar el servicio MySQL
2. Iniciar sesión en la base de datos (user: root, password: Según el sistema)
3. En consulta SQL, ejecutar el script para la creación de la base de datos vacía: `CREATE DATABASE streamflow;`
4. En Intellij IDEA (o IDE de preferencia), abrir el proyecto Stream-Flow, dirigirse al archivo `Streamflow.Application.java` y correrlo.
5. **Nota sobre los datos:** El proyecto está configurado con `ddl-auto` y un archivo `data.sql`. Al correr la aplicación, las tablas se crearán y se insertarán datos iniciales de prueba de manera automática.
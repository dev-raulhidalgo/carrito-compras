# Practica1DSS — Catálogo de productos con carrito y seguridad

Práctica 1 de **Desarrollo de Sistemas Software (DSS)** — Universidad de Granada.

| | |
|---|---|
| **Autor** | Raúl Hidalgo Véliz |
| **Correo** | [raulhidalgo@correo.ugr.es](mailto:raulhidalgo@correo.ugr.es) |
| **Proyecto Maven** | `dss.example:pr1` (`0.0.1-SNAPSHOT`) |
| **Stack** | Java 17, Spring Boot 3.5.9, Maven, Thymeleaf, Spring Data JPA, Spring Security, H2 |

Aplicación web de catálogo de productos con CRUD (solo administrador), carrito de compra por sesión, autenticación y exportación SQL de la base de datos.

---

## Requisitos

- **JDK 17** (o compatible)
- **Maven 3.x**, o el wrapper incluido (`mvnw` / `mvnw.cmd`)

No hace falta instalar un servidor de bases de datos: H2 se ejecuta en memoria.

---

## Cómo ejecutar

Desde la raíz del proyecto:

```bash
./mvnw spring-boot:run
```

En Windows:

```bash
mvnw.cmd spring-boot:run
```

También se puede importar el proyecto en Spring Tools / Eclipse y arrancar `dss.example.pr1.Pr1Application`.

La aplicación queda en **http://localhost:8080**.

### Tests

```bash
./mvnw test
```

Las pruebas de autorización, CSRF, catálogo, exportación y carrito por sesión están en `src/test/java/dss/example/pr1/SecurityAndCartTests.java`.

---

## Usuarios de demostración

Definidos en `SecurityConfig` (no se usa `spring.security.user.*` de Spring Boot).

| Usuario | Contraseña | Rol | Qué puede hacer |
|---------|------------|-----|-----------------|
| `admin` | `admin` | `ADMIN` | Catálogo, panel `/admin`, alta/edición/borrado, exportar SQL |
| `user`  | `user`  | `USER`  | Ver catálogo, usar el carrito. Recibe **403** en gestión y `/admin` |

Las contraseñas van con `{noop}` (sin cifrar) porque es un entorno de práctica local.

---

## Rutas principales

| Método | Ruta | Acceso | Descripción |
|--------|------|--------|-------------|
| GET | `/` | Público | Página de inicio |
| GET | `/login` | Público | Formulario de login |
| POST | `/logout` | Autenticado | Cierre de sesión (redirige a `/login?logout`) |
| GET | `/products` | Público | Listado del catálogo (búsqueda y filtro por precio) |
| GET | `/products/add` | ADMIN | Formulario de alta |
| GET | `/products/edit/{id}` | ADMIN | Formulario de edición |
| POST | `/products/save` | ADMIN | Guardar producto (CSRF) |
| POST | `/products/delete/{id}` | ADMIN | Eliminar producto (CSRF) |
| GET | `/cart` | Público | Ver carrito de la sesión |
| POST | `/cart/add/{id}` | Público | Añadir al carrito (CSRF) |
| POST | `/cart/remove/{id}` | Público | Quitar del carrito (CSRF) |
| GET | `/admin` | ADMIN | Panel de administración |
| GET | `/admin/export` | ADMIN | Descarga `products.sql` |
| GET | `/h2-console` | Público (desarrollo) | Consola H2 |

Solo **GET `/products`** es público del catálogo. Las rutas de gestión no usan `permitAll` sobre `/products/**`.

Los botones de alta, edición y borrado en `productos.html` se muestran solo a usuarios con rol ADMIN (`sec:authorize`).

---

## Funcionalidades

### Catálogo (CRUD)

- Entidad `Product`: `id`, `name`, `price` e `imageUrl` (opcional).
- Repositorio `ProductRepo` (`JpaRepository`).
- Servicio `ProductService` con listado, búsqueda, alta, edición y baja.
- Plantillas Thymeleaf: `productos.html` y `formulario-producto.html`.
- Datos iniciales en `src/main/resources/data.sql`: kiwi (1,63 €) y crema (5,78 €).

### Búsqueda y filtro (mejora opcional)

En `/products` se puede filtrar por:

- `q` — texto en el nombre
- `min` / `max` — rango de precio (inclusive)

### Imágenes

El administrador puede subir una imagen al guardar el producto (jpg, jpeg, png, gif, webp; máximo 5 MB). Se almacenan en la carpeta `uploads/` y se sirven en `/uploads/**`.

### Carrito por sesión

`CartService` está anotado con `@SessionScope`: cada sesión HTTP tiene su propio carrito. Añadir y quitar se hace con POST (token CSRF de Thymeleaf + Spring Security).

### Seguridad

- Spring Security con login propio (`/login`).
- CSRF activo salvo en la consola H2.
- Frames de H2 con `sameOrigin`.
- Anónimo que intenta gestionar el catálogo es redirigido al login.
- Usuario `USER` recibe 403 en gestión y en `/admin/**`.

### Exportación SQL

`GET /admin/export` genera un fichero `products.sql` con `CREATE TABLE` e `INSERT` por cada producto (`Content-Disposition: attachment; filename=products.sql`).

---

## Configuración (`application.properties`)

- Base de datos H2 en memoria: `jdbc:h2:mem:testdb`
- Usuario H2: `sa` / contraseña: `password`
- Consola H2: `spring.h2.console.enabled=true` → http://localhost:8080/h2-console  
  JDBC URL: `jdbc:h2:mem:testdb`
- JPA: `ddl-auto=update` y `defer-datasource-initialization=true` para cargar `data.sql`
- Thymeleaf sin caché (desarrollo)

Los usuarios de la aplicación **no** se configuran aquí; solo en `SecurityConfig`.

---

## Estructura del código

```
Practica1DSS/
├── pom.xml
├── README.md
├── src/main/java/dss/example/pr1/
│   ├── Pr1Application.java
│   ├── config/WebConfig.java          # recursos /uploads/**
│   ├── controller/
│   │   ├── HomeController.java
│   │   ├── LoginController.java
│   │   ├── ProductController.java
│   │   ├── CartController.java
│   │   └── AdminController.java
│   ├── model/Product.java
│   ├── repository/ProductRepo.java
│   ├── security/SecurityConfig.java
│   └── service/
│       ├── ProductService.java
│       ├── CartService.java           # @SessionScope
│       └── DatabaseExportService.java
├── src/main/resources/
│   ├── application.properties
│   ├── data.sql
│   └── templates/                     # Thymeleaf
│       ├── index.html
│       ├── login.html
│       ├── productos.html
│       ├── formulario-producto.html
│       ├── cart.html
│       └── admin.html
└── src/test/java/dss/example/pr1/
    ├── Pr1ApplicationTests.java
    └── SecurityAndCartTests.java
```

---

## Dependencias Maven (resumen)

- `spring-boot-starter-web`
- `spring-boot-starter-thymeleaf`
- `thymeleaf-extras-springsecurity6`
- `spring-boot-starter-data-jpa`
- `h2` (runtime)
- `spring-boot-starter-security`
- `spring-boot-starter-test` y `spring-security-test` (tests)

Lombok no se usa: getters y setters son manuales.

---

## Contacto

Raúl Hidalgo — [raulhidalgo@correo.ugr.es](mailto:raulhidalgo@correo.ugr.es)

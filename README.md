# ProjectBar

**ProjectBar** es un sistema de gestión integral para bares y restaurantes, diseñado para **optimizar la atención al cliente y agilizar el trabajo de los meseros**.

## ¿Por qué ProjectBar?

En la industria de la hospitalidad, el tiempo es clave. ProjectBar nace con el objetivo de:

- **Reducir tiempos de espera**: Los clientes pueden realizar pedidos de forma más rápida y eficiente.
- **Facilitar el trabajo de los meseros**: Gestión centralizada de órdenes, mesas y facturación desde cualquier dispositivo.
- **Minimizar errores**: Sistema digital que elimina confusiones en pedidos escritos a mano.
- **Mejorar la experiencia del cliente**: Atención más ágil y personalizada.

## Características

- Gestión de productos, ingredientes e inventario
- Creación y seguimiento de órdenes con ciclo de vida completo
- Generación de facturas en PDF (JasperReports)
- Autenticación y autorización por roles con JWT
- API REST documentada con Swagger/OpenAPI

## Tecnologías

| Categoría         | Tecnología                     | Versión |
| ----------------- | ------------------------------ | ------- |
| Lenguaje          | Java                           | 17      |
| Framework         | Spring Boot                    | 3.3.1   |
| Persistencia      | Spring Data JPA + Hibernate    | -       |
| Base de datos     | MySQL                          | 8       |
| Seguridad         | Spring Security + JWT (jjwt)   | 0.11.5  |
| Reportes PDF      | JasperReports                  | 7.0.3   |
| Mapeo de objetos  | MapStruct                      | 1.5.5   |
| Documentación API | springdoc-openapi (Swagger UI) | 2.1.0   |
| Validación        | Spring Boot Starter Validation | -       |
| Testing           | JUnit 5 + Mockito              | -       |
| Cobertura         | JaCoCo                         | 0.8.12  |
| Build             | Maven                          | -       |

## Requisitos previos

- Java 17 o superior
- Maven 3.6+
- MySQL 8

## Configuración

1. Copia el archivo de variables de entorno:

```bash
cp .env.example .env
```

2. Edita el `.env` con tus valores:

```env
DB_URL=jdbc:mysql://localhost:3306/bar_db
DB_USERNAME=root
DB_PASSWORD=root
JWT_SECRET_KEY=<clave-hexadecimal-de-256-bits>
JWT_EXPIRATION_TIME=3600000
SHOW_SQL=true
```

3. Crea la base de datos en MySQL:

```sql
CREATE DATABASE bar_db;
```

> Hibernate creará y actualizará las tablas automáticamente al iniciar la aplicación (`ddl-auto: update`).

## Ejecución

```bash
./mvnw spring-boot:run
```

La aplicación queda disponible en `http://localhost:8090`.

## Comandos útiles

| Comando                     | Descripción                           |
| --------------------------- | ------------------------------------- |
| `./mvnw spring-boot:run`    | Ejecutar la aplicación                |
| `./mvnw clean install`      | Compilar y empaquetar                 |
| `./mvnw test`               | Ejecutar pruebas unitarias            |
| `./mvnw test jacoco:report` | Pruebas + reporte de cobertura JaCoCo |
| `./mvnw clean package`      | Generar JAR en `target/`              |

El reporte de cobertura HTML se genera en `target/site/jacoco/index.html`.

## Documentación de la API

La documentación interactiva está disponible en **Swagger UI** una vez la aplicación esté corriendo:

```
http://localhost:8090/swagger-ui/index.html
```

### Endpoints

#### Autenticación — `/api/auth`

| Método | Ruta              | Acceso      | Descripción                    |
| ------ | ----------------- | ----------- | ------------------------------ |
| POST   | `/api/auth/login` | Público     | Obtener token JWT              |
| GET    | `/api/auth/me`    | Autenticado | Información del usuario actual |

#### Productos — `/api/products`

| Método | Ruta                                | Acceso  | Descripción                 |
| ------ | ----------------------------------- | ------- | --------------------------- |
| GET    | `/api/products`                     | Público | Listar todos los productos  |
| GET    | `/api/products/{id}`                | Público | Obtener producto por ID     |
| GET    | `/api/products/code/{code}`         | Público | Obtener por código          |
| GET    | `/api/products/name/{name}`         | Público | Buscar por nombre exacto    |
| GET    | `/api/products/category/{category}` | Público | Filtrar por categoría       |
| GET    | `/api/products/search/{name}`       | Público | Búsqueda parcial por nombre |
| POST   | `/api/products`                     | ADMIN   | Crear producto              |
| PUT    | `/api/products/{id}`                | ADMIN   | Actualizar producto         |
| DELETE | `/api/products/code/{code}`         | ADMIN   | Eliminar producto           |

#### Órdenes — `/api/orders`

| Método | Ruta                                                | Acceso                      | Descripción                              |
| ------ | --------------------------------------------------- | --------------------------- | ---------------------------------------- |
| POST   | `/api/orders`                                       | Público                     | Crear orden (autoservicio QR)            |
| GET    | `/api/orders`                                       | ADMIN/BARTENDER/WAITER/CHEF | Listar todas (paginado)                  |
| GET    | `/api/orders/{id}`                                  | Autenticado                 | Obtener por ID                           |
| GET    | `/api/orders/client/{name}`                         | Autenticado                 | Buscar por nombre de cliente             |
| GET    | `/api/orders/table/{numberTable}`                   | Autenticado                 | Buscar por mesa                          |
| GET    | `/api/orders/waiter/{id}`                           | ADMIN                       | Buscar por mesero                        |
| GET    | `/api/orders/mine`                                  | Autenticado                 | Órdenes del mesero actual                |
| GET    | `/api/orders/mine/date-range`                       | Autenticado                 | Órdenes por rango de fechas              |
| GET    | `/api/orders/mine/assigned`                         | Autenticado                 | Órdenes ASIGNADAS al mesero              |
| GET    | `/api/orders/unassigned`                            | ADMIN                       | Órdenes en estado CREATED                |
| GET    | `/api/orders/date/{date}`                           | Autenticado                 | Buscar por fecha                         |
| GET    | `/api/orders/date-range`                            | Autenticado                 | Buscar por rango de fechas (paginado)    |
| GET    | `/api/orders/table/{tableNumber}/grouped-by-client` | Autenticado                 | Órdenes pendientes agrupadas por cliente |
| PUT    | `/api/orders/{id}`                                  | ADMIN/WAITER                | Actualizar orden                         |
| PATCH  | `/api/orders/{id}/items`                            | ADMIN/WAITER                | Agregar ítem a la orden                  |
| DELETE | `/api/orders/{id}/items/{itemId}`                   | ADMIN/WAITER                | Eliminar ítem de la orden                |
| PATCH  | `/api/orders/{id}/status`                           | ADMIN/BARTENDER/WAITER/CHEF | Cambiar estado de la orden               |
| PATCH  | `/api/orders/{id}/waiter/{waiterUsername}`          | ADMIN                       | Asignar mesero a la orden                |
| DELETE | `/api/orders/{id}`                                  | ADMIN                       | Eliminar orden                           |

#### Ingredientes — `/api/ingredients`

| Método | Ruta                           | Acceso     | Descripción            |
| ------ | ------------------------------ | ---------- | ---------------------- |
| GET    | `/api/ingredients`             | ADMIN/CHEF | Listar todos           |
| GET    | `/api/ingredients/{id}`        | ADMIN/CHEF | Obtener por ID         |
| GET    | `/api/ingredients/code/{code}` | ADMIN/CHEF | Obtener por código     |
| POST   | `/api/ingredients`             | ADMIN      | Crear ingrediente      |
| PUT    | `/api/ingredients/{id}`        | ADMIN      | Actualizar ingrediente |
| DELETE | `/api/ingredients/code/{code}` | ADMIN      | Eliminar ingrediente   |

#### Inventario — `/api/inventory`

| Método | Ruta                                 | Acceso                      | Descripción                       |
| ------ | ------------------------------------ | --------------------------- | --------------------------------- |
| GET    | `/api/inventory`                     | ADMIN/BARTENDER/WAITER/CHEF | Listar todo el inventario         |
| GET    | `/api/inventory/{code}`              | Autenticado                 | Obtener por código de ingrediente |
| POST   | `/api/inventory`                     | ADMIN                       | Crear entrada de inventario       |
| PATCH  | `/api/inventory/{code}/add-stock`    | ADMIN                       | Agregar stock                     |
| PATCH  | `/api/inventory/{code}/deduct-stock` | ADMIN                       | Descontar stock                   |
| DELETE | `/api/inventory/{code}`              | ADMIN                       | Eliminar entrada                  |

#### Facturas — `/api/bills`

| Método | Ruta                                          | Acceso       | Descripción                                 |
| ------ | --------------------------------------------- | ------------ | ------------------------------------------- |
| GET    | `/api/bills`                                  | ADMIN        | Listar facturas (paginado + filtros)        |
| GET    | `/api/bills/{id}/pdf`                         | ADMIN        | Descargar factura en PDF                    |
| POST   | `/api/bills/table/{numberTable}/{clientName}` | ADMIN/WAITER | Generar factura por mesa y cliente          |
| POST   | `/api/bills/client/{clientName}`              | ADMIN/WAITER | Generar factura por nombre de cliente       |
| POST   | `/api/bills/selection`                        | ADMIN/WAITER | Generar factura desde órdenes seleccionadas |

#### Mesas — `/api/tables`

| Método | Ruta                          | Acceso                      | Descripción                                 |
| ------ | ----------------------------- | --------------------------- | ------------------------------------------- |
| GET    | `/api/tables`                 | ADMIN/BARTENDER/WAITER/CHEF | Listar todas las mesas                      |
| GET    | `/api/tables/{id}`            | Autenticado                 | Obtener por ID                              |
| GET    | `/api/tables/number/{number}` | Autenticado                 | Obtener por número de mesa                  |
| GET    | `/api/tables/status/{status}` | Autenticado                 | Filtrar por estado (FREE/OCCUPIED/RESERVED) |
| POST   | `/api/tables`                 | ADMIN                       | Crear mesa                                  |
| PUT    | `/api/tables/{id}`            | ADMIN                       | Actualizar mesa                             |
| PATCH  | `/api/tables/{id}/status`     | ADMIN/WAITER                | Cambiar estado de la mesa                   |
| DELETE | `/api/tables/{id}`            | ADMIN                       | Eliminar mesa                               |

#### Ítems de orden — `/api/order-items`

| Método | Ruta               | Acceso      | Descripción                       |
| ------ | ------------------ | ----------- | --------------------------------- |
| POST   | `/api/order-items` | Autenticado | Crear ítem de orden independiente |

#### Meseros — `/api/waiters`

| Método | Ruta           | Acceso | Descripción                                  |
| ------ | -------------- | ------ | -------------------------------------------- |
| GET    | `/api/waiters` | ADMIN  | Listar meseros con conteo de órdenes activas |

## Seguridad

- **Autenticación:** Tokens JWT Bearer (HMAC-SHA256), emitidos en `/api/auth/login`
- **Sesiones:** Sin estado (stateless) — no se almacena sesión en el servidor
- **Contraseñas:** Codificadas con BCrypt
- **CORS:** Allows `localhost:4200`, `localhost:5173`, `localhost:5174`, `localhost:3000`
- **Endpoints públicos:** `GET /api/products/**`, `POST /api/orders`, `POST /api/auth/login`, Swagger UI

### Roles

| Rol         | Descripción                                 |
| ----------- | ------------------------------------------- |
| `ADMIN`     | Acceso completo                             |
| `WAITER`    | Gestión de órdenes y mesas                  |
| `BARTENDER` | Vista de órdenes e inventario               |
| `CHEF`      | Vista de órdenes, ingredientes e inventario |

## Ciclo de vida de una orden

```
Orden por QR (cliente):  CREATED → ASSIGNED → IN_PROGRESS → READY → DELIVERED → BILLED
Orden por mesero:                              IN_PROGRESS → READY → DELIVERED → BILLED
Cancelación:             Cualquier estado → CANCELLED
```

## Estructura del proyecto

```
src/
├── main/java/com/app/projectbar/
│   ├── application/
│   │   ├── exception/        # Excepciones de dominio personalizadas
│   │   ├── implementation/   # Implementaciones de servicios
│   │   ├── interfaces/       # Contratos de servicio
│   │   └── mapper/           # Mappers MapStruct
│   ├── config/               # Seguridad, JWT y Swagger
│   ├── domain/               # Entidades, DTOs y enums
│   └── infra/
│       ├── controller/       # Controladores REST
│       ├── errorHandler/     # Manejo global de excepciones
│       └── repositories/     # Repositorios Spring Data JPA
└── test/                     # Pruebas unitarias (JUnit 5 + Mockito)
```

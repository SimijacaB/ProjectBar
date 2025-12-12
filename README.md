# ProjectBar

ProjectBar es una aplicación backend desarrollada con Java y Spring Boot para gestión de un bar/venta de productos: manejo de productos, inventario, órdenes, facturación y generación de reportes (facturas PDF).

Este repositorio contiene el servidor REST, configuración de seguridad, integración para generación de reportes (plantillas Jasper) y ejemplos estáticos de PDF en `src/main/resources/static`.

Características principales
- Gestión de productos, ingredientes y relaciones entre productos e ingredientes.
- Control de inventario.
- Creación y gestión de órdenes y facturas.
- Generación de facturas/reportes en PDF usando plantillas Jasper (`src/main/resources/reports/templates`).
- Seguridad y gestión de usuarios/roles (clases `UserEntity`, `UserRoleEntity`).
- Documentación de API vía Swagger/OpenAPI (configuración en `config/SwaggerConfig.java`).

Tecnologías
- Java 17 (o superior)
- Spring Boot
- Maven (con `mvnw` incluido)
- JasperReports para generación de PDFs
- Spring Security
- ModelMapper (configuración en `config/ModelMapperConfig.java`)

Requisitos
- JDK 17+
- Maven 3.6+ (o usar el wrapper `./mvnw`)

Instalación y ejecución local
1. Clonar el repositorio (si no lo has hecho):

```bash
git clone <repo-url>
cd ProjectBar
```

2. Construir el proyecto:

```bash
./mvnw clean package
```

3. Ejecutar usando Maven (en desarrollo):

```bash
./mvnw spring-boot:run
```

4. Ejecutar el JAR empaquetado:

```bash
java -jar target/*.jar
```

Comandos útiles
- Ejecutar pruebas:

```bash
./mvnw test
```

- Limpiar y compilar:

```bash
./mvnw clean install
```

Configuración
- El archivo principal de configuración está en `src/main/resources/application.yml` (también se encuentra la copia en `target/classes/application.yml` tras el build).
- Variables de entorno y propiedades comunes:
  - `SPRING_PROFILES_ACTIVE` — perfil activo.
  - `SERVER_PORT` — puerto del servidor.
  - Parámetros de conexión a la base de datos (configurados en `application.yml`).

Documentación de la API
- El proyecto incluye configuración de Swagger/OpenAPI. Abre la URL de Swagger en tu navegador para ver todos los endpoints y probarlos:
  - Posibles URLs: `http://localhost:8080/swagger-ui.html` o `http://localhost:8080/swagger-ui/index.html` (dependiendo de la versión de Springdoc/Swagger usada en el proyecto).
- También revisa los controladores en `src/main/java/com/app/projectbar/infra/controller` para ver rutas expuestas.

Reportes y facturas
- Plantillas Jasper: `src/main/resources/reports/templates/invoice.jrxml` y `invoice.jasper`.
- PDFs de ejemplo: `src/main/resources/static/Factura_1.pdf`, `Factura_5.pdf`, `ReportGenerated.pdf`.


## Endpoints
A continuación se listan los endpoints principales expuestos por la API (ruta, método, descripción y cuerpo/parametros esperados).

Notas generales:
- Todos los endpoints consumen y producen JSON salvo los que explicitamente devuelven PDF (`application/pdf`).
- Los DTOs que aparecen en las firmas (por ejemplo `ProductRequestDTO`) están en `src/main/java/com/app/projectbar/domain/dto`.
- Muchos endpoints están protegidos por seguridad; revisa `config/SecurityConfig.java` para detalles de autenticación/autorización.
- Para fechas (LocalDate) se espera el formato ISO: `yyyy-MM-dd`.


### ProductController (base: /api/product)
- POST /api/product/save
  - Descripción: Crear un producto.
  - Body: `ProductRequestDTO`
  - Respuesta: `ProductResponseDTO`

- PUT /api/product/update
  - Descripción: Actualizar un producto.
  - Body: `UpdateProductRequestDTO`
  - Respuesta: `ProductResponseDTO`

- GET /api/product/all
  - Descripción: Listar productos.
  - Respuesta: `List<ProductForListResponseDTO>`

- GET /api/product/{id}
  - Descripción: Obtener producto por id.
  - Path: `id` (Long)
  - Respuesta: `ProductResponseDTO`

- GET /api/product/find-by-code/{code}
  - Descripción: Obtener producto por código.
  - Path: `code` (String)
  - Respuesta: `ProductResponseDTO`

- GET /api/product/find-by-name/{name}
  - Descripción: Buscar productos por nombre (parcial/igual según implementación).
  - Path: `name` (String)
  - Respuesta: `List<ProductForListResponseDTO>`

- GET /api/product/find-by-category/{category}
  - Descripción: Filtrar por categoría (enum `Category`).
  - Path: `category` (String — se convierte a enum, pasar la clave de categoría en mayúsculas o en el valor esperado).
  - Respuesta: `List<ProductForListResponseDTO>`

- DELETE /api/product/delete/{code}
  - Descripción: Eliminar producto por código.
  - Path: `code` (String)


### OrderController (base: /api/order)
- POST /api/order/save
  - Descripción: Crear una orden (transaccional).
  - Body: `OrderRequestDTO`
  - Respuesta: `OrderResponseDTO`

- GET /api/order/all
  - Descripción: Listar todas las órdenes.
  - Respuesta: `List<OrderForListResponseDTO>`

- GET /api/order/find-by-client-name/{name}
  - Descripción: Buscar órdenes por nombre de cliente.
  - Path: `name` (String)
  - Respuesta: `List<OrderForListResponseDTO>`

- GET /api/order/find-by-table-number/{numberTable}
  - Descripción: Buscar órdenes por número de mesa.
  - Path: `numberTable` (Integer)
  - Respuesta: `List<OrderForListResponseDTO>`

- GET /api/order/find-by-waiter-id/{id}
  - Descripción: Buscar órdenes por id de mesero/usuario.
  - Path: `id` (String)
  - Respuesta: `List<OrderForListResponseDTO>`

- GET /api/order/find-by-date/{date}
  - Descripción: Buscar órdenes por fecha (ISO `yyyy-MM-dd`).
  - Path: `date` (LocalDate)
  - Respuesta: `List<OrderForListResponseDTO>`

- PUT /api/order/update
  - Descripción: Actualizar una orden (transaccional).
  - Body: `UpdateOrderDTO`
  - Respuesta: `OrderResponseDTO`

- GET /api/order/find-by-id/{id}
  - Descripción: Obtener orden por id.
  - Path: `id` (Long)
  - Respuesta: `OrderResponseDTO`

- PUT /api/order/add-order-item/{idOrder}
  - Descripción: Añadir un item a la orden (transaccional).
  - Path: `idOrder` (Long)
  - Body: `OrderItemRequestDTO`
  - Respuesta: `OrderResponseDTO`

- PUT /api/order/remove-order-item/{idOrder}/{idOrderItem}/{quantityToRemove}
  - Descripción: Eliminar/cantidad de un item en la orden (transaccional).
  - Path: `idOrder` (Long), `idOrderItem` (Long), `quantityToRemove` (Integer)
  - Respuesta: `OrderResponseDTO`

- PUT /api/order/change-status/{idOrder}/{status}
  - Descripción: Cambiar el estado de la orden (transaccional).
  - Path: `idOrder` (Long), `status` (String)
  - Respuesta: `OrderResponseDTO`

- DELETE /api/order/delete/{id}
  - Descripción: Eliminar una orden por id (transaccional).
  - Path: `id` (Long)


### OrderItemController (base: /api/order-item)
- POST /api/order-item/save
  - Descripción: Crear un item de orden.
  - Body: `OrderItemRequestDTO`
  - Respuesta: `OrderItemResponseDTO`


### IngredientController (base: /api/ingredient)
- GET /api/ingredient/{id}
  - Descripción: Obtener ingrediente por id.
  - Path: `id` (Long)
  - Respuesta: `IngredientResponseDTO`

- GET /api/ingredient/all
  - Descripción: Listar ingredientes.
  - Respuesta: `List<IngredientResponseDTO>`

- GET /api/ingredient/code/{code}
  - Descripción: Obtener ingrediente por código.
  - Path: `code` (String)
  - Respuesta: `IngredientResponseDTO`

- POST /api/ingredient/save
  - Descripción: Crear ingrediente (transaccional).
  - Body: `IngredientRequestDTO`
  - Respuesta: `IngredientResponseDTO`

- PUT /api/ingredient/update
  - Descripción: Actualizar ingrediente (transaccional).
  - Body: `UpdateIngredientDTO`
  - Respuesta: `IngredientResponseDTO`

- DELETE /api/ingredient/delete/{code}
  - Descripción: Eliminar ingrediente por código (transaccional).
  - Path: `code` (String)


### InventoryController (base: /api/inventory)
- POST /api/inventory/save
  - Descripción: Crear entrada de inventario (transaccional).
  - Body: `InventoryDTO`
  - Respuesta: `InventoryResponseDTO`

- PUT /api/inventory/add-stock/{quantity}/{code}
  - Descripción: Agregar stock a un inventario identificado por `code` (transaccional).
  - Path: `quantity` (Integer), `code` (String)
  - Respuesta: `InventoryResponseDTO`

- PUT /api/inventory/deduct-stock/{quantity}/{code}
  - Descripción: Deduct stock de un inventario (transaccional).
  - Path: `quantity` (Integer), `code` (String)
  - Respuesta: `InventoryResponseDTO`

- GET /api/inventory/all
  - Descripción: Listar inventarios.
  - Respuesta: `List<InventoryResponseDTO>`

- GET /api/inventory/find-by-code/{code}
  - Descripción: Buscar inventario por código.
  - Path: `code` (String)
  - Respuesta: `InventoryResponseDTO`

- DELETE /api/inventory/delete/{code}
  - Descripción: Eliminar inventario por código (transaccional).
  - Path: `code` (String)


### BillController (base: /api/bill)
- POST /api/bill/save/by-table/{numberTable}/{clientName}
  - Descripción: Generar factura para una mesa y cliente (transaccional).
  - Path: `numberTable` (Integer), `clientName` (String)
  - Respuesta: `BillDTO`

- POST /api/bill/save/by-client/{clientName}
  - Descripción: Generar factura por cliente (transaccional).
  - Path: `clientName` (String)
  - Respuesta: `BillDTO`

- POST /api/bill/save/by-selection
  - Descripción: Generar factura a partir de una selección de órdenes.
  - Body: `OrdersForBillDto` (contiene lista de `ordersId`)
  - Respuesta: `BillDTO`

- GET /api/bill/all
  - Descripción: Listar facturas.
  - Respuesta: `List<BillDTO>`

- GET /api/bill/download-pdf/{billId}
  - Descripción: Descargar la factura en PDF.
  - Path: `billId` (Long)
  - Respuesta: `application/pdf` attachment (bytes del PDF)


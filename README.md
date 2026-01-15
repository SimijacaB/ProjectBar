# ProjectBar

**ProjectBar** es un sistema de gestión integral para bares también pensado en implementar en restaurantes, diseñado para **optimizar la atención al cliente y agilizar el trabajo de los meseros**.

##  ¿Por qué ProjectBar?

En la industria de la hospitalidad, el tiempo es clave. ProjectBar nace con el objetivo de:

- **Reducir tiempos de espera**: Los clientes pueden realizar pedidos de forma más rápida y eficiente.
- **Facilitar el trabajo de los meseros**: Gestión centralizada de órdenes, mesas y facturación desde cualquier dispositivo.
- **Minimizar errores**: Sistema digital que elimina confusiones en pedidos escritos a mano.
- **Mejorar la experiencia del cliente**: Atención más ágil y personalizada.

##  Características

- Gestión de productos, ingredientes e inventario
- Creación y gestión de órdenes y facturas
- Generación de facturas en PDF (JasperReports)
- Seguridad y gestión de usuarios/roles
- API REST documentada con Swagger/OpenAPI

##  Tecnologías

- Java 17+
- Spring Boot
- Spring Security
- Maven
- JasperReports
- ModelMapper

##  Documentación de la API

La documentación interactiva está disponible en **Swagger UI**:

```
http://localhost:8080/swagger-ui.html
```

### Controladores principales

| Controlador          | Base Path         | Descripción                      |
| -------------------- | ----------------- | -------------------------------- |
| ProductController    | `/api/product`    | Gestión de productos             |
| OrderController      | `/api/order`      | Gestión de órdenes               |
| OrderItemController  | `/api/order-item` | Items de órdenes                 |
| IngredientController | `/api/ingredient` | Gestión de ingredientes          |
| InventoryController  | `/api/inventory`  | Control de inventario            |
| BillController       | `/api/bill`       | Facturación y generación de PDFs |

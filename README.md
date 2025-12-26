# ProjectBar – Backend API

ProjectBar es una aplicación backend desarrollada con **Java 17 y Spring Boot** orientada a la gestión operativa de un bar.  
El proyecto comenzó como un sistema de administración interna y ha evolucionado hacia una **API de pedidos digitales**, diseñada para optimizar la atención en escenarios de alta demanda.

La solución permite que los clientes realicen pedidos desde su mesa y que el personal del bar gestione y entregue los pedidos de forma eficiente, reduciendo tiempos de espera y carga operativa para los meseros.

---

## 🧩 Alcance del proyecto

- Gestión de productos e ingredientes.
- Control de inventario.
- Creación y gestión de pedidos y facturación.
- Flujo de pedidos orientado a clientes y meseros.
- Generación de facturas y reportes en PDF.
- Gestión de usuarios y roles.
- API REST documentada mediante Swagger/OpenAPI.

El proyecto se encuentra **en desarrollo activo**, siendo utilizado como base para aplicar mejoras continuas en lógica de negocio, validaciones y buenas prácticas backend.

---

## ⚙️ Características principales

- Arquitectura en capas (**Controller – Service – Repository**).
- Persistencia de datos con **Spring Data JPA** y **MySQL**.
- Generación de reportes y facturas en PDF usando **JasperReports**.
- Seguridad y gestión de usuarios/roles mediante **Spring Security**.
- Mapeo de DTOs con **ModelMapper**.
- Documentación automática de la API con **Swagger/OpenAPI**.

---

## 🛠 Tecnologías utilizadas

- Java 17
- Spring Boot
- Spring Data JPA
- Spring Security
- MySQL
- Maven (incluye `mvnw`)
- JasperReports
- Swagger / OpenAPI
- ModelMapper

---

## 📋 Requisitos

- JDK 17 o superior
- Maven 3.6 o superior
- Base de datos MySQL

---

## 📖 Documentación de la API

El proyecto incluye documentación interactiva de la API mediante Swagger/OpenAPI.

Una vez ejecutada la aplicación, puedes acceder a:

- http://localhost:8080/swagger-ui.html  
- http://localhost:8080/swagger-ui/index.html  

(La URL puede variar según la versión de Springdoc configurada).

Los controladores REST se encuentran en:


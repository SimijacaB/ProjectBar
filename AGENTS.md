# AGENTS.md — ProjectBar

## Stack verified facts

- **Java 17** + **Spring Boot 3.3.1** + **Maven** (wrapper: `./mvnw`)
- **MySQL 8** on port **3306** (not default 3306 → check `application.yml`)
- **App port: 8090** (not 8080)
- **Build tool**: `./mvnw` (not `mvn`)
- **Frontend is SEPARATE** — this repo is backend only

## Commands

```bash
./mvnw spring-boot:run          # Run app (port 8090)
./mvnw clean install             # Compile + package
./mvnw test                      # Unit tests (JUnit 5 + Mockito)
./mvnw test jacoco:report       # Tests + coverage report (target/site/jacoco/)
```

## Profiles & Config

- **dev** (default): `ddl-auto: update`, `show-sql: true`
- **prod**: `ddl-auto: validate`, `show-sql: false`
- Activate: `SPRING_PROFILES_ACTIVE=prod ./mvnw spring-boot:run`
- JWT secret must be **256-bit hex** (`openssl rand -hex 32`)

## Architecture Gotchas

### Lazy Loading / JPA
- **Always use `JOIN FETCH`** in repo queries when you need related entities (e.g., `Product.productIngredients`)
- Example fixed in `IProductRepository.findById()` — without it, `LazyInitializationException`

### MapStruct + Lombok
- Both need annotation processing (configured in `pom.xml` `maven-compiler-plugin`)
- Mappers are interfaces with `@Mapper(componentModel = "spring")`
- **If mapper ignores a field** (like `ingredients`), update the mapper, not the service

### Testing
- Tests are **unit tests with Mockito** (`@Mock` + `@InjectMocks`), NOT Spring Boot test slices
- No integration tests — no running MySQL required for `./mvnw test`
- Test files: `src/test/java/com/app/projectbar/application/implementation/`

### Code Patterns
- **Layered**: Controller → Service (interface + impl) → Repository
- **DTOs**: Request/Response DTOs in `domain/dto/`, mapping in `application/mapper/`
- **Update pattern**: `saveOrUpdate()` method preserves existing data when fields are null
- **Product with ingredients**: `UpdateProductRequestDTO` → `ProductRequestDTO` via mapper (was ignoring ingredients, now fixed)

## Security

- **JWT Bearer** tokens (HMAC-SHA256), endpoint: `POST /api/auth/login`
- **Roles**: `ADMIN` (full), `WAITER`, `BARTENDER`, `CHEF` (see `domain/enums/Role.java`)
- **Public endpoints**: `GET /api/products/**`, `POST /api/orders`, `POST /api/auth/login`, Swagger UI
- **CORS**: whitelists `localhost:4200`, `:5173`, `:5174`, `:3000`

## API Docs

- Swagger UI: `http://localhost:8090/swagger-ui/index.html`
- Auth required for most endpoints (except public ones above)

## JasperReports PDF

- Templates: `src/main/resources/reports/templates/`
- Generated PDFs: `src/main/resources/static/`

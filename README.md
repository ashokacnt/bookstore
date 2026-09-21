# bookstore — Online Book Store

A professional, modular Spring Boot 3.4.3 online bookstore application using Java 17, Maven, Spring Security Basic Authentication, Spring Data JPA, H2, validation, Swagger/OpenAPI, centralized error handling, request correlation, logging, idempotent order checkout, and automated tests.

---

## 1. Project Overview

`bookstore` is a simple online bookstore backend that supports:

- User registration
- Basic Authentication
- USER and ADMIN roles
- Public book listing
- ADMIN-only book creation
- Shopping cart management
- Order checkout
- Transactional stock validation and reduction
- Order history
- Idempotent order creation
- Centralized validation and error handling
- Custom 401/403 security responses
- Request correlation using `X-Request-Id`
- Application and repository logging
- Swagger/OpenAPI documentation
- Unit, controller, security, and repository tests
- JaCoCo code coverage

The application is intentionally implemented without JWT. It uses HTTP Basic Authentication.

---

## 2. Technology Stack

| Technology | Version / Purpose |
|---|---|
| Java | 17 |
| Spring Boot | 3.4.3 |
| Maven | Build and dependency management |
| Spring Web | REST APIs |
| Spring Security | Basic Authentication and authorization |
| Spring Data JPA | Persistence |
| Hibernate | JPA implementation |
| H2 | Development/test database |
| Jakarta Validation | Request validation |
| Springdoc OpenAPI | Swagger UI |
| JUnit 5 | Testing |
| Mockito | Unit testing |
| MockMvc | Controller/API testing |
| Spring Security Test | Security testing |
| JaCoCo | Code coverage |
| Logback | Application logging |
| Git | Version control |

---

## 3. Architecture

The project follows a module-wise layered architecture.

```text
bookstore
├── auth
│   ├── controller
│   ├── dto
│   ├── entity
│   ├── repository
│   └── service
│
├── book
│   ├── controller
│   ├── dto
│   ├── entity
│   ├── repository
│   └── service
│
├── cart
│   ├── controller
│   ├── dto
│   ├── entity
│   ├── repository
│   └── service
│
├── order
│   ├── controller
│   ├── dto
│   ├── entity
│   ├── repository
│   └── service
│
├── common
│   ├── exception
│   ├── handler
│   ├── logging
│   └── response
│
└── config
    ├── security
    └── swagger
    
```

Typical request flow:

```text
Client
  |
  v
Controller
  |
  v
Service
  |
  v
Repository
  |
  v
H2 Database
```

Security flow:

```text
HTTP Basic Credentials
        |
        v
Spring Security
        |
        +---- Invalid / Missing Credentials ---> 401
        |
        +---- Valid USER calling ADMIN API --> 403
        |
        +---- Valid ADMIN ------------------> Controller
```

---

## 4. Prerequisites

Install:

- Java 17 or later
- Maven 3.8+
- Git
- IDE such as IntelliJ IDEA, Eclipse, or VS Code

Verify Java:

```bash
java -version
```

Verify Maven:

```bash
mvn -version
```

---

## 5. Clone, Build and Run

Clone the project:

```bash
git clone <your-repository-url>
cd bookstore
```

Build:

```bash
mvn clean install
```

Run with Maven:

```bash
mvn spring-boot:run
```

Or build and run the JAR:

```bash
mvn clean package
java -jar target/bookstore-0.0.1-SNAPSHOT.jar
```

The application starts on:

```text
http://localhost:8081
```

---

## 6. Configuration

The application uses `application.properties`.

Example:

```properties
server.port=8081

spring.datasource.url=jdbc:h2:mem:bookstore;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
spring.datasource.username=sa
spring.datasource.password=

spring.jpa.hibernate.ddl-auto=create-drop

app.admin.bootstrap-enabled=true
app.admin.username=admin
app.admin.password=${BOOKSTORE_ADMIN_PASSWORD:admin123}

logging.level.root=INFO
logging.file.name=${user.home}/bookstore/logs/bookstore.log
```

### Important

The H2 database is in-memory, so data is recreated when the application restarts.

For production, use a persistent database such as PostgreSQL, MySQL, or Oracle and use Flyway/Liquibase for schema management.

---

## 7. Default Development Users

The development setup provides:

| Username | Password | Role |
|---|---|---|
| demo | password | USER |
| admin | admin123 | ADMIN |

### Security behavior

- Missing or invalid credentials → `401 UNAUTHORIZED`
- Authenticated USER accessing an ADMIN-only API → `403 FORBIDDEN`
- Authenticated ADMIN accessing an ADMIN-only API → permitted

Do not use these default credentials in production.

The public registration API creates users with the `USER` role. A client must not be allowed to register itself as ADMIN.

---

## 8. H2 Console

H2 console:

```text
http://localhost:8081/h2-console
```

Connection details:

```text
JDBC URL: jdbc:h2:mem:bookstore
User Name: sa
Password:
```

The H2 console is intended only for development.

Disable it in production.

---

## 9. Swagger / OpenAPI

Swagger UI:

```text
http://localhost:8081/swagger-ui.html
```

Alternative:

```text
http://localhost:8081/swagger-ui/index.html
```

OpenAPI JSON:

```text
http://localhost:8081/v3/api-docs
```

Swagger provides an interactive way to inspect and execute the REST APIs.

For protected APIs, provide valid Basic Authentication credentials.

---

# 10. API Summary

| Method | Endpoint | Authentication | Purpose |
|---|---|---|---|
| POST | `/api/auth/register` | Public | Register USER |
| GET | `/api/books` | Public | List books |
| POST | `/api/books` | ADMIN | Add a book |
| GET | `/api/cart` | Authenticated | View cart |
| POST | `/api/cart/items` | Authenticated | Add item |
| PUT | `/api/cart/items/{itemId}` | Authenticated | Update quantity |
| DELETE | `/api/cart/items/{itemId}` | Authenticated | Remove item |
| POST | `/api/orders` | Authenticated | Checkout cart |
| GET | `/api/orders` | Authenticated | Order history |

---

# 11. API Usage — User Registration

### Request

```http
POST http://localhost:8081/api/auth/register
Content-Type: application/json
```

Request body:

```json
{
  "username": "ashok",
  "password": "password"
}
```

### Successful response

HTTP `201 Created`

```json
{
  "id": 3,
  "username": "ashok",
  "role": "USER"
}
```

The registration API does not accept an ADMIN role from the client.

---

# 12. API Usage — Get Books

Book listing is public.

### Request

```http
GET http://localhost:8081/api/books
```

No authentication is required.

### Successful response

HTTP `200 OK`

Example:

```json
[
  {
    "id": 1,
    "title": "Clean Code",
    "author": "Robert C. Martin",
    "isbn": "9780132350884",
    "price": 750.00,
    "stockQuantity": 10
  },
  {
    "id": 2,
    "title": "Effective Java",
    "author": "Joshua Bloch",
    "isbn": "9780134685991",
    "price": 850.00,
    "stockQuantity": 20
  }
]
```

---

# 13. API Usage — Add Book

Only ADMIN users can add books.

### Request

```http
POST http://localhost:8081/api/books
Authorization: Basic <admin-credentials>
Content-Type: application/json
```

Example request body:

```json
{
  "title": "Effective Java",
  "author": "Joshua Bloch",
  "isbn": "9780134685991",
  "price": 850.00,
  "stockQuantity": 20
}
```

### Validation rules

- `title` must not be blank
- `author` must not be blank
- `isbn` must not be blank
- `isbn` must be unique
- `price` must be greater than zero
- `stockQuantity` must be zero or greater

### Successful response

HTTP `201 Created`

```json
{
  "id": 2,
  "title": "Effective Java",
  "author": "Joshua Bloch",
  "isbn": "9780134685991",
  "price": 850.00,
  "stockQuantity": 20
}
```

# 14. API Usage — Shopping Cart

All cart APIs require authentication.

## 14.1 View Cart

```http
GET http://localhost:8081/api/cart
Authorization: Basic <credentials>
```

Example response:

```json
{
  "items": [
    {
      "itemId": 1,
      "bookId": 2,
      "title": "Effective Java",
      "quantity": 2,
      "unitPrice": 850.00,
      "subtotal": 1700.00
    }
  ],
  "totalAmount": 1700.00
}
```

---

## 14.2 Add Item to Cart

```http
POST http://localhost:8081/api/cart/items
Authorization: Basic <credentials>
Content-Type: application/json
```

Request:

```json
{
  "bookId": 2,
  "quantity": 2
}
```

The backend validates that the book exists and that the requested quantity is valid.

The cart API does not use the order idempotency key.

---

## 14.3 Update Cart Item

Example:

```http
PUT http://localhost:8081/api/cart/items/1?quantity=3
Authorization: Basic <credentials>
```

The quantity is validated by the backend.

---

## 14.4 Remove Cart Item

```http
DELETE http://localhost:8081/api/cart/items/1
Authorization: Basic <credentials>
```

Expected response:

```text
204 No Content
```

---

# 15. API Usage — Checkout

Checkout converts the current user's cart into an order.

### Request

```http
POST http://localhost:8081/api/orders
Authorization: Basic <credentials>
Idempotency-Key: demo-order-001
```

There is no request body because the order is created from the authenticated user's current cart.

### Checkout flow

```text
1. Authenticate user
2. Read user's cart
3. Validate cart is not empty
4. Validate every book exists
5. Validate stock availability
6. Create order
7. Create order items
8. Snapshot current book prices
9. Reduce stock
10. Clear cart
11. Return order response
```

The checkout operation should be transactional so that a failure does not leave partially completed data.

### Example response

```json
{
  "orderId": 1,
  "status": "CREATED",
  "totalAmount": 1700.00
}
```

---

# 16. Idempotency

Idempotency is implemented for:

```http
POST /api/orders
```

The client sends:

```http
Idempotency-Key: demo-order-001
```

### Why is this useful?

Imagine the client sends an order request and the network connection fails before the response reaches the client.

The client may retry.

Without idempotency:

```text
Request 1 -> Order 101
Request 2 -> Order 102
```

The customer could accidentally receive two orders.

With idempotency:

```text
Request 1 + demo-order-001 -> Order 101
Request 2 + demo-order-001 -> Existing Order 101
```

The backend returns the original result instead of creating a duplicate order.

### Who generates the key?

The client generates the idempotency key.

Examples:

```text
demo-order-001
order-<uuid>
checkout-550e8400-e29b-41d4-a716-446655440000
```

The backend stores and validates the key.

### Important rules

- Missing key → `400 Bad Request`
- Same key + same operation → return original result
- Same key + conflicting operation → `409 Conflict`
- A different key can create a different order

For production, idempotency records should be stored in a persistent and preferably distributed store when multiple application instances are running.

---

# 17. API Usage — Order History

```http
GET http://localhost:8081/api/orders
Authorization: Basic <credentials>
```

Example:

```json
[
  {
    "orderId": 1,
    "status": "CREATED",
    "totalAmount": 1700.00
  }
]
```

Only orders belonging to the authenticated user should be returned.

---

# 18. Standard Error Response

The application uses a consistent error response structure.

Example:

```json
{
  "status": 400,
  "code": "BAD_REQUEST",
  "error": "Bad Request",
  "message": "Cart is empty.",
  "path": "/api/orders",
  "fieldErrors": []
}
```

Validation errors may contain field-level details:

```json
{
  "status": 400,
  "code": "VALIDATION_FAILED",
  "error": "Bad Request",
  "message": "Request validation failed.",
  "path": "/api/books",
  "fieldErrors": [
    {
      "field": "price",
      "message": "must be greater than 0"
    }
  ]
}
```

---

# 19. HTTP 400 — Bad Request

Typical scenarios:

- Missing required request field
- Invalid quantity
- Invalid price
- Invalid registration request
- Missing `Idempotency-Key`
- Empty cart checkout

Example:

```json
{
  "status": 400,
  "code": "BAD_REQUEST",
  "error": "Bad Request",
  "message": "Cart is empty.",
  "path": "/api/orders",
  "fieldErrors": []
}
```

---

# 20. HTTP 401 — Unauthorized

Occurs when authentication is required but credentials are missing or invalid.

Example:

```json
{
  "status": 401,
  "code": "UNAUTHORIZED",
  "error": "Unauthorized",
  "message": "Authentication is required to access this resource. Please provide valid username and password.",
  "path": "/api/books",
  "fieldErrors": []
}
```

For example, calling:

```http
POST /api/books
```

without valid credentials should return `401`.

This response is handled by the application's `SecurityErrorHandler` / authentication entry point rather than only by the controller exception handler.

---

# 21. HTTP 403 — Forbidden

Occurs when the user is authenticated but does not have the required role.

Example:

```json
{
  "status": 403,
  "code": "FORBIDDEN",
  "error": "Forbidden",
  "message": "You are not authorized to perform this operation. ADMIN role is required to add books.",
  "path": "/api/books",
  "fieldErrors": []
}
```

Example:

```text
demo/password -> USER
POST /api/books -> 403
```

Whereas:

```text
admin/admin123 -> ADMIN
POST /api/books -> 201
```

---

# 22. HTTP 404 — Not Found

Typical scenarios:

- Book does not exist
- Cart item does not exist
- Requested order does not exist

Example:

```json
{
  "status": 404,
  "code": "NOT_FOUND",
  "error": "Not Found",
  "message": "Book with id 9999 was not found.",
  "path": "/api/books/9999",
  "fieldErrors": []
}
```

---

# 23. HTTP 409 — Conflict

Typical scenarios:

- Duplicate ISBN
- Duplicate/conflicting idempotency key
- Insufficient stock where the application treats the business conflict as a resource state conflict

Example:

```json
{
  "status": 409,
  "code": "CONFLICT",
  "error": "Conflict",
  "message": "A book with ISBN 9780134685991 already exists.",
  "path": "/api/books",
  "fieldErrors": []
}
```

---

# 24. HTTP 500 — Internal Server Error

Unexpected server-side failures should not expose stack traces or sensitive internal details to API clients.

Example:

```json
{
  "status": 500,
  "code": "INTERNAL_SERVER_ERROR",
  "error": "Internal Server Error",
  "message": "An unexpected error occurred.",
  "path": "/api/orders",
  "fieldErrors": []
}
```

The detailed exception should be logged on the server.

---

# 25. Request Correlation

The application supports request correlation using:

```http
X-Request-Id
```

Example:

```http
X-Request-Id: 8c5a0a35-6f2a-4b0d-9b61-123456789abc
```

If the client does not provide a request ID, the application generates one.

The same request ID can be returned in the response and included in logs.

This makes it easier to trace:

```text
HTTP request
   |
Controller
   |
Service
   |
Repository
   |
Exception
```

without exposing sensitive information.

---

# 26. Logging

The project uses Spring Boot's default Logback-based logging.

It does not require a separate Log4j2 configuration.

Default level:

```text
INFO
```

Log file:

```text
${user.home}/bookstore/logs/bookstore.log
```

On Windows this will normally resolve under the current user's home directory.

Logging includes:

- Request start/end
- Request ID
- Controller flow
- Service flow
- Repository execution
- Important business events
- Exceptions

Do not log:

- Passwords
- Authorization headers
- Basic Authentication credentials
- Sensitive personal information

---

# 27. Testing

The project should contain tests at multiple levels.

## Service unit tests

Use Mockito:

```java
@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository repository;

    @InjectMocks
    private BookServiceImpl service;
}
```

## Controller tests

Spring Boot 3.4.x tests should use `@MockitoBean` instead of deprecated `@MockBean`.

Example:

```java
@WebMvcTest(BookController.class)
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookService bookService;
}
```

Import:

```java
import org.springframework.test.context.bean.override.mockito.MockitoBean;
```

## Repository tests

Use:

```java
@DataJpaTest
```

## Security tests

Cover:

- Public GET books
- Missing credentials → 401
- Invalid credentials → 401
- USER attempting admin operation → 403
- ADMIN adding a book → 201

## Run tests

```bash
mvn test
```

## JaCoCo

Generate coverage:

```bash
mvn verify
```

The coverage report is normally generated under:

```text
target/site/jacoco/index.html
```

---

# 28. requests.http

The project includes:

```text
requests.http
```

This file contains executable HTTP examples for tools such as IntelliJ IDEA and compatible REST clients.

Typical scenarios include:

- Register user
- Get books
- Add book as ADMIN
- Attempt add book without authentication
- Attempt add book as USER
- Add cart item
- Update cart item
- Remove cart item
- Checkout
- Repeat checkout with the same idempotency key
- Get order history

It is useful for manual API verification without needing Postman.

---

# 29. Documentation Files

The project documentation is separated by purpose.

```text
README.md
docs/
├── architecture.md
├── api-documentation.md
└── error-scenarios.md
```

### README.md

Provides:

- Project overview
- Technologies
- Setup
- Main APIs
- Authentication
- Error handling
- Testing
- Logging
- Troubleshooting
- Future enhancements

### docs/architecture.md

Provides:

- Architecture
- Module responsibilities
- Request flow
- Security flow
- Data flow
- Transaction boundaries
- Future modules

### docs/api-documentation.md

Provides detailed API information:

- URL
- HTTP method
- Authentication
- Headers
- Request body
- Response body
- HTTP status
- Validation rules

### docs/error-scenarios.md

Provides positive and negative API scenarios and expected errors.

---

# 30. End-to-End Example

A typical user journey is:

```text
1. Start application
        |
2. Register USER
        |
3. Login using Basic Authentication
        |
4. GET /api/books
        |
5. POST /api/cart/items
        |
6. GET /api/cart
        |
7. PUT /api/cart/items/{itemId}
        |
8. POST /api/orders
        |
9. Send Idempotency-Key
        |
10. Checkout validates stock
        |
11. Order created
        |
12. Stock reduced
        |
13. Cart cleared
        |
14. GET /api/orders
```

ADMIN journey:

```text
ADMIN credentials
       |
       v
POST /api/books
       |
       v
Book validation
       |
       +---- Invalid ---> 400
       |
       +---- Duplicate ISBN ---> 409
       |
       +---- Valid ---> 201
```

---

# 31. Future Enhancements

Possible future modules include:

- Payment integration
- Book categories
- Book search
- Pagination and sorting
- Wishlist
- Reviews and ratings
- Inventory management
- Order cancellation
- Order status lifecycle
- Email notifications
- Admin dashboard
- Redis caching
- Distributed idempotency
- PostgreSQL/MySQL/Oracle
- Flyway/Liquibase database migrations
- Docker
- CI/CD pipeline
- Spring Boot Actuator
- Micrometer
- Prometheus/Grafana monitoring
- Distributed tracing
- API rate limiting
- Production-grade audit logging

---

# 32. Production Considerations

Before deploying to production:

- Disable H2 console
- Disable admin bootstrap
- Do not use default passwords
- Store secrets in a secure secret manager
- Use a persistent production database
- Use Flyway or Liquibase
- Enable HTTPS
- Configure secure CORS policies
- Configure security headers
- Add rate limiting
- Add monitoring and alerting
- Add audit logging
- Review database indexes
- Configure connection pooling
- Use a persistent/distributed idempotency store
- Avoid logging sensitive information
- Review authentication and authorization rules
- Add automated security testing

---

# 33. Troubleshooting

## Java version problem

Verify:

```bash
java -version
```

The project targets Java 17.

## Port 8081 already in use

Change:

```properties
server.port=8081
```

to another available port, for example:

```properties
server.port=8082
```

## ADMIN receives 401

Check:

1. Admin bootstrap is enabled.
2. Username is `admin`.
3. Password is `admin123`, unless overridden by `BOOKSTORE_ADMIN_PASSWORD`.
4. The Basic Authentication header is being sent.
5. The application was restarted after configuration changes.

## USER receives 403

This is expected when a valid USER calls an ADMIN-only endpoint.

For example:

```text
demo/password
POST /api/books
```

should result in:

```text
403 FORBIDDEN
```

## Duplicate ISBN

A duplicate ISBN should result in:

```text
409 CONFLICT
```

Use a different ISBN.

## Cart accepts a nonexistent book

The service layer must verify the book exists before creating a cart item.

A nonexistent book should result in:

```text
404 NOT_FOUND
```

## Checkout fails because of stock

The checkout service should validate stock before reducing inventory.

The checkout transaction should roll back if a later operation fails.

---

# 34. Important Security Design Notes

The application deliberately uses Basic Authentication instead of JWT.

Basic Authentication is appropriate for this learning/demo application because it keeps the security implementation straightforward.

For production:

- Always use HTTPS.
- Use strong passwords.
- Prefer secure password hashing such as BCrypt.
- Do not expose credentials in logs.
- Consider OAuth2/OIDC or another appropriate authentication architecture for larger distributed systems.

The ADMIN role must be assigned by trusted backend processes or administrators and must never be accepted directly from public registration input.

---

# 35. License

This project is intended as a demo application.

Add the organization's approved license before distributing it as an open-source or commercial project.

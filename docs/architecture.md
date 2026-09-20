# bookstore Architecture

## 1. Overview

`bookstore` is designed as a modular Spring Boot REST application for an online bookstore.

The architecture separates business capabilities into modules while keeping shared concerns such as security, exception handling, validation, logging, and configuration reusable.

Major planned modules:

- Auth
- Book
- Cart
- Order
- Common
- Config

## 2. High-Level Architecture

```text
Client
  |
  v
REST Controllers
  |
  v
Service Layer
  |
  v
Repository Layer
  |
  v
Database
```

Cross-cutting concerns include security, validation, exception handling, logging, request correlation, transactions, and API documentation.

## 3. Module-Wise Architecture

### 3.1 Auth Module

Package:

```text
com.cg.auth
```

Responsibilities:

- User registration
- User authentication
- User roles
- User details lookup
- Password handling

Expected components:

```text
auth/
├── controller/
├── service/
├── repository/
├── entity/
└── dto/
```

The application will use Spring Security Basic Authentication.

Public registration must not allow a client to assign the `ADMIN` role to itself.

### 3.2 Book Module

Package:

```text
com.cg.book
```

Responsibilities:

- List books
- Add books
- Update book information
- Manage inventory
- Validate book information

Expected components:

```text
book/
├── controller/
├── service/
├── repository/
├── entity/
└── dto/
```

Access model:

```text
GET /api/books
    -> Public

POST /api/books
    -> ADMIN only
```

Authorization should be explicit by HTTP method so that making GET public does not accidentally expose POST.

### 3.3 Cart Module

Package:

```text
com.cg.cart
```

Responsibilities:

- Add book to cart
- Update cart quantity
- Remove cart item
- Retrieve current user's cart

Expected components:

```text
cart/
├── controller/
├── service/
├── repository/
├── entity/
└── dto/
```

Cart operations are associated with the authenticated user.

### 3.4 Order Module

Package:

```text
com.cg.order
```

Responsibilities:

- Create orders
- Checkout from cart
- Validate stock
- Reduce stock
- Clear cart
- Retrieve order history
- Maintain order status
- Provide idempotent checkout

Expected components:

```text
order/
├── controller/
├── service/
├── repository/
├── entity/
└── dto/
```

Checkout will be transactional so order creation, stock reduction, and cart clearing are treated as one business operation.

## 4. Common Module

Package:

```text
com.cg.common
```

Responsibilities:

- Global exception handling
- Standard API error responses
- Validation error responses
- Request correlation
- Shared utilities

Expected structure:

```text
common/
├── exception/
├── error/
├── logging/
└── util/
```

## 5. Config Module

Package:

```text
com.cg.config
```

Responsibilities:

- Spring Security configuration
- Application configuration
- OpenAPI configuration
- Infrastructure configuration

Expected structure:

```text
config/
├── security/
├── openapi/
└── application/
```

## 6. Security Architecture

Spring Security will protect authenticated APIs using Basic Authentication.

Planned authorization model:

```text
Public
  |
  +-- GET /api/books

Authenticated USER
  |
  +-- Cart APIs
  +-- Order APIs
  +-- User-specific APIs

ADMIN
  |
  +-- Administrative book operations
```

Security responsibilities include authentication, authorization, role-based access, HTTP 401 handling, and HTTP 403 handling.

A dedicated `SecurityErrorHandler` will handle authentication and authorization failures so security errors remain separate from application-level exceptions.

## 7. Exception Handling

The application will use centralized exception handling.

Expected HTTP status categories:

```text
400 Bad Request
401 Unauthorized
403 Forbidden
404 Not Found
409 Conflict
500 Internal Server Error
```

Application exceptions will be handled by a global exception handler.

Security exceptions will be handled by the security error handler.

## 8. Validation

Request DTOs will use Jakarta Bean Validation where appropriate.

Examples:

```text
@NotBlank
@NotNull
@Positive
@PositiveOrZero
```

Validation failures will return a consistent error response containing field-level validation details.

Business validations such as duplicate ISBN and insufficient stock will be handled in the service layer.

## 9. Logging

The application will use Spring Boot's default Logback implementation.

Logging objectives:

- Request tracing
- Controller activity
- Service-layer business operations
- Repository execution logging
- Error logging

A request ID will be generated or accepted from the `X-Request-Id` header and stored using MDC.

Sensitive information such as passwords and authorization credentials must never be logged.

Application logs are intended to use:

```text
${user.home}/bookstore/logs/bookstore.log
```

## 10. Transaction Management

Order checkout is a business transaction.

Planned flow:

```text
1. Validate authenticated user
2. Load current cart
3. Validate cart is not empty
4. Validate books and stock
5. Create order
6. Create order items
7. Snapshot item prices
8. Reduce stock
9. Clear cart
10. Commit transaction
```

If an error occurs, database changes should roll back so order, inventory, and cart remain consistent.

## 11. Idempotency

Order creation will support an `Idempotency-Key` request header.

```text
Client
  |
  | POST /api/orders
  | Idempotency-Key: ABC123
  v
Order Service
  |
  +-- First request -> Create order
  |
  +-- Same key + same request -> Return original result
  |
  +-- Same key + different request -> 409 Conflict
```

Idempotency is intended for checkout/order creation and is not required for ordinary cart operations.

## 12. Testing Architecture

### Unit Tests

Service classes will be tested using JUnit 5 and Mockito.

### Controller Tests

REST endpoints will be tested using MockMvc and Spring Security Test.

Security scenarios include:

```text
No credentials -> 401
Invalid credentials -> 401
Authenticated USER -> 403 for ADMIN endpoint
Authenticated ADMIN -> successful ADMIN operation
```

### Repository Tests

Persistence behavior will be tested using:

```text
@DataJpaTest
```

H2 will be used for development and test execution.

### Integration Tests

Important business flows will be tested end-to-end, especially registration, authentication, book creation, cart operations, checkout, stock reduction, idempotency, and error handling.

## 13. Design Principles

### Separation of Concerns

Controllers handle HTTP concerns.

Services contain business logic.

Repositories handle persistence.

### Dependency Inversion

Business services depend on repository abstractions rather than database implementation details.

### Single Responsibility

Each class should have one clear responsibility.

### Explicit Authorization

Security rules should clearly express which users can access each endpoint.

### Consistent Errors

All application errors should use a predictable JSON structure.

### Transactional Consistency

Operations that must succeed or fail together should be executed inside a transaction.

### Testability

Business logic should be structured so it can be tested independently.

### Maintainability

The codebase should favor clear module boundaries and readable naming over unnecessary complexity.

## 14. Future Evolution

Potential future modules include:

```text
Payment
Customer Profile
Wishlist
Review and Rating
Inventory Management
Admin Dashboard
Notification
Search
Product Categories
Reporting
```

The initial implementation intentionally keeps the architecture simple while providing clear extension points for future capabilities.

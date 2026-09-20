# bookstore

## Project Overview

`bookstore` is a Spring Boot based Online Book Store application designed using a clean, module-oriented architecture.

The project is being developed incrementally using small, logical Git commits. The architecture is intended to support authentication, book management, shopping cart, order management, validation, centralized error handling, logging, API documentation, and automated testing.

At this stage, the project contains the initial Spring Boot foundation and project documentation. Functional modules will be added in subsequent commits.

## Technology Stack

| Technology | Version / Usage |
|---|---|
| Java | 17 |
| Spring Boot | 3.4.3 |
| Maven | Build and dependency management |
| Spring Web | REST APIs |
| Spring Data JPA | Persistence |
| Hibernate | ORM |
| H2 | Development and test database |
| Spring Security | Basic Authentication |
| JUnit 5 | Unit and integration testing |
| Mockito | Mock-based unit testing |
| MockMvc | REST controller testing |
| Swagger / OpenAPI | API documentation |
| JaCoCo | Code coverage |
| Logback | Application logging |

## Development Approach

The project follows a modular architecture so that each business capability can evolve independently.

Planned modules include:

- `auth` - user registration and authentication
- `book` - book management
- `cart` - shopping cart management
- `order` - order and checkout management
- `common` - shared exceptions, error responses, utilities, and logging
- `config` - application and security configuration

The implementation is developed incrementally through focused Git commits.

## How to Run

### Prerequisites

Install:

- Java 17 or later
- Maven 3.9+ recommended
- Git

Verify:

```bash
java -version
mvn -version
git --version
```

### Clone the Project

```bash
git clone <repository-url>
cd bookstore6
```

### Build

```bash
mvn clean install
```

### Run

```bash
mvn spring-boot:run
```

Or:

```bash
mvn clean package
java -jar target/bookstore6-*.jar
```

The application will normally start on:

```text
http://localhost:8080
```

## Project Structure

```text
bookstore/
├── README.md
├── requests.http
├── docs/
│   ├── architecture.md
│   ├── api-documentation.md
│   └── error-scenarios.md
└── src/
    ├── main/
    │   ├── java/
    │   │   └── com/cg/
    │   │       ├── auth/
    │   │       ├── book/
    │   │       ├── cart/
    │   │       ├── order/
    │   │       ├── common/
    │   │       └── config/
    │   └── resources/
    │       └── application.properties
    └── test/
        └── java/
            └── com/cg/
```

## Module Responsibilities

### Auth
User registration, authentication, and roles.

### Book
Book listing, administration, validation, and stock management.

### Cart
Add, update, remove, and view cart items.

### Order
Checkout, order history, transactional processing, stock validation, and idempotency.

### Common
Shared exceptions, error responses, validation support, request correlation, and utilities.

### Config
Security, database, OpenAPI, and application configuration.

## API Documentation

Detailed API documentation will be maintained in:

```text
docs/api-documentation.md
```

Executable request examples will be maintained in:

```text
requests.http
```

## Testing Strategy

The project will use:

- JUnit 5
- Mockito
- MockMvc
- Spring Security Test
- `@DataJpaTest`
- Integration tests
- JaCoCo

## Future Modules and Features

1. User registration
2. Basic Authentication
3. USER and ADMIN roles
4. Book management
5. Role-based book creation
6. Centralized exception handling
7. Shopping cart
8. Order management
9. Transactional checkout
10. Stock validation and reduction
11. Order idempotency
12. Request correlation and application logging
13. Repository execution logging
14. Request validation
15. Swagger / OpenAPI documentation
16. Unit and integration tests
17. JaCoCo code coverage
18. Detailed API and error documentation

## Git Commit Strategy

The project is developed using focused commits. Each commit represents one logical change.

Example:

```text
chore: initialize bookstore6 spring boot project
docs: add project overview and architecture
config: add h2 database and jpa configuration
feat: add user entity and repository
feat: add user registration api
...
```

## Current Status

Current commit:

```text
docs: add project overview and architecture
```

The application foundation and architecture documentation are being established first. Functional modules will be implemented in subsequent commits.

## License

This project is intended for learning, interview preparation, and demonstration of enterprise Java/Spring Boot development practices.

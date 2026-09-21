# bookstorev6 Error and Negative Scenarios

All error responses use the common structure:

```json
{
  "status": 400,
  "code": "VALIDATION_ERROR",
  "error": "Bad Request",
  "message": "Request validation failed",
  "path": "/api/books",
  "fieldErrors": [
    {
      "field": "title",
      "message": "must not be blank"
    }
  ]
}
```

## 1. Authentication and authorization

### 1.1 No credentials on protected API

```http
GET /api/cart
```

Expected: **401 Unauthorized**.

```json
{
  "status": 401,
  "code": "UNAUTHORIZED",
  "error": "Unauthorized",
  "message": "Authentication is required to access this resource. Please provide valid username and password.",
  "path": "/api/cart",
  "fieldErrors": []
}
```

### 1.2 Invalid username/password

Use an invalid Basic Authentication credential on a protected API.

Expected: **401 Unauthorized** with the same security error structure.

### 1.3 USER attempts ADMIN operation

```http
POST /api/books
Authorization: Basic <demo-credentials>
```

Expected: **403 Forbidden**.

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

This 401 vs 403 distinction is implemented by `SecurityErrorHandler` because Spring Security rejects the request before controller-level exception handling.

## 2. Registration failures

| Scenario | Request | Expected |
|---|---|---:|
| Blank username | `username: ""` | 400 |
| Blank password | `password: ""` | 400 |
| Missing username | omit field | 400 |
| Missing password | omit field | 400 |
| Duplicate username | existing username | 409 |

Example duplicate response:

```json
{
  "status": 409,
  "code": "CONFLICT",
  "error": "Conflict",
  "message": "Username already exists",
  "path": "/api/auth/register",
  "fieldErrors": []
}
```

## 3. Book failures

### 3.1 Missing title

```json
{
  "author": "Joshua Bloch",
  "isbn": "9780134685991",
  "price": 850.00,
  "stockQuantity": 20
}
```

Expected: **400 VALIDATION_ERROR**.

### 3.2 Missing author

Expected: **400 VALIDATION_ERROR**.

### 3.3 Missing ISBN

Expected: **400 VALIDATION_ERROR**.

### 3.4 Price is zero or negative

```json
{
  "title": "Java Book",
  "author": "Author",
  "isbn": "9781111111111",
  "price": 0,
  "stockQuantity": 10
}
```

Expected: **400 VALIDATION_ERROR**.

### 3.5 Negative stock

```json
{
  "title": "Java Book",
  "author": "Author",
  "isbn": "9781111111112",
  "price": 500.00,
  "stockQuantity": -1
}
```

Expected: **400 VALIDATION_ERROR**.

### 3.6 Duplicate ISBN

Use an ISBN already stored in the database.

Expected: **409 CONFLICT**.

## 4. Cart failures

| Scenario | Expected |
|---|---:|
| No authentication | 401 |
| Book ID is missing | 400 |
| Quantity is 0 | 400 |
| Quantity is negative | 400 |
| Book does not exist | 404 |
| Cart item does not belong to current user | 404/business failure |
| Update with quantity 0 | 400 |
| Delete non-existing cart item | 404 |

Example add-to-cart validation failure:

```json
{
  "bookId": 2,
  "quantity": 0
}
```

Expected: **400 VALIDATION_ERROR**.

Example missing book:

```json
{
  "bookId": 999999,
  "quantity": 1
}
```

Expected: **404 NOT_FOUND**.

## 5. Checkout failures

### 5.1 Missing Idempotency-Key

```http
POST /api/orders
Authorization: Basic <user-credentials>
Content-Type: application/json
```

Expected: **400** because the controller requires the `Idempotency-Key` header.

Depending on Spring MVC's required-header handling, this missing-header case is handled by the application's global error mapping or the framework response. The intended API contract is `400 Bad Request`.

### 5.2 Empty cart

Send a valid authenticated checkout request while the user's cart is empty.

Expected: **400 Bad Request**.

### 5.3 Insufficient stock

Put more quantity in the cart than the current stock allows.

Expected: business failure, normally **400/409** according to the service exception used by the project.

The order must not be created and stock must not be partially decremented.

### 5.4 Same idempotency key for same user

Repeat the same checkout with the same key.

Expected: the original order response is returned and a duplicate order is not created.

### 5.5 Same idempotency key for another user

Attempt to reuse a key already associated with another authenticated user.

Expected: **409 CONFLICT**.

Example:

```json
{
  "status": 409,
  "code": "CONFLICT",
  "error": "Conflict",
  "message": "Idempotency-Key is already associated with another user",
  "path": "/api/orders",
  "fieldErrors": []
}
```

## 6. Malformed JSON

Example:

```json
{
  "title": "Java Book",
  "price":
}
```

Expected: **400 BAD_REQUEST**.

```json
{
  "status": 400,
  "code": "BAD_REQUEST",
  "error": "Bad Request",
  "message": "Request body is missing or contains invalid JSON/data.",
  "path": "/api/books",
  "fieldErrors": []
}
```

## 7. Unknown resource / URL

Examples:

```text
GET /api/does-not-exist
PUT /api/cart/items/999999
```

Expected resource-not-found behavior is **404**. Exact framework handling for an unmapped URL can depend on the application's `/error` handling; resource lookups from the service layer use the project's `NOT_FOUND` error structure.

## 8. Unexpected server error

Unexpected exceptions are mapped by `GlobalExceptionHandler` to:

```json
{
  "status": 500,
  "code": "INTERNAL_SERVER_ERROR",
  "error": "Internal Server Error",
  "message": "Unexpected server error",
  "path": "/api/example",
  "fieldErrors": []
}
```

## Recommended security test matrix

| Request | No Auth | USER | ADMIN |
|---|---:|---:|---:|
| GET `/api/books` | 200 | 200 | 200 |
| POST `/api/books` | 401 | 403 | 201 |
| GET `/api/cart` | 401 | 200 | 200 |
| POST `/api/cart/items` | 401 | 201 | 201 |
| POST `/api/orders` | 401 | 201* | 201* |

`201*` assumes the authenticated user's cart is non-empty, stock is sufficient, and a valid unique `Idempotency-Key` is supplied.

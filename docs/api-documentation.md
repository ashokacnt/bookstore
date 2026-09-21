# bookstorev6 API Documentation

Base URL:

```text
http://localhost:8081
```

Authentication uses **HTTP Basic Authentication**. There is no JWT in this project.

## Development credentials

```text
ADMIN
username: admin
password: admin123

USER
username: demo
password: password
```

## Common headers

For JSON requests:

```http
Content-Type: application/json
Accept: application/json
```

For authenticated APIs:

```http
Authorization: Basic <base64(username:password)>
```

You can configure Basic Auth directly in Postman/Insomnia instead of manually calculating Base64.

## 1. List books

### Request

```http
GET /api/books
Accept: application/json
```

Authentication: **not required**.

### Success — 200

```json
[
  {
    "id": 1,
    "title": "Clean Code",
    "author": "Robert C. Martin",
    "isbn": "9780132350884",
    "price": 650.00,
    "stockQuantity": 10,
    "active": true
  }
]
```

If there are no books:

```json
[]
```

---

## 2. Register user

### Request

```http
POST /api/auth/register
Content-Type: application/json
Accept: application/json
```

Body:

```json
{
  "username": "alice",
  "password": "password123"
}
```

Registration always creates a normal `USER`; the client cannot choose `ADMIN`.

### Success — 201

```json
{
  "id": 2,
  "username": "alice",
  "role": "USER"
}
```

---

## 3. Add book

### Request

```http
POST /api/books
Authorization: Basic <admin-credentials>
Content-Type: application/json
Accept: application/json
```

Body:

```json
{
  "title": "Effective Java",
  "author": "Joshua Bloch",
  "isbn": "9780134685991",
  "price": 850.00,
  "stockQuantity": 20
}
```

Authentication: **ADMIN required**.

### Success — 201

```json
{
  "id": 2,
  "title": "Effective Java",
  "author": "Joshua Bloch",
  "isbn": "9780134685991",
  "price": 850.00,
  "stockQuantity": 20,
  "active": true
}
```

---

## 4. Get current user's cart

### Request

```http
GET /api/cart
Authorization: Basic <user-credentials>
Accept: application/json
```

Authentication: **USER or ADMIN**.

### Success — 200

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
  "total": 1700.00
}
```

Empty cart:

```json
{
  "items": [],
  "total": 0.00
}
```

---

## 5. Add item to cart

### Request

```http
POST /api/cart/items
Authorization: Basic <user-credentials>
Content-Type: application/json
Accept: application/json
```

Body:

```json
{
  "bookId": 2,
  "quantity": 2
}
```

### Success — 201

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
  "total": 1700.00
}
```

---

## 6. Update cart item

### Request

```http
PUT /api/cart/items/1
Authorization: Basic <user-credentials>
Content-Type: application/json
Accept: application/json
```

Body:

```json
{
  "quantity": 5
}
```

### Success — 200

```json
{
  "items": [
    {
      "itemId": 1,
      "bookId": 2,
      "title": "Effective Java",
      "quantity": 5,
      "unitPrice": 850.00,
      "subtotal": 4250.00
    }
  ],
  "total": 4250.00
}
```

---

## 7. Remove cart item

### Request

```http
DELETE /api/cart/items/1
Authorization: Basic <user-credentials>
```

### Success — 204

No response body.

---

## 8. Checkout

Checkout uses the current authenticated user's cart.

### Request

```http
POST /api/orders
Authorization: Basic <user-credentials>
Content-Type: application/json
Accept: application/json
Idempotency-Key: 7e5b4c8a-2c1e-4f6b-9a10-123456789abc
```

The request body is not used by the controller; an empty JSON object can be sent:

```json
{}
```

### Success — 201

```json
{
  "orderId": 1,
  "total": 1700.00,
  "status": "CREATED",
  "createdAt": "2026-09-20T15:30:00",
  "items": [
    {
      "bookId": 2,
      "title": "Effective Java",
      "quantity": 2,
      "unitPrice": 850.00,
      "subtotal": 1700.00
    }
  ]
}
```

Successful checkout decreases book stock and clears the user's cart.

### Retry with same Idempotency-Key

Send the same authenticated request with the same key. The application returns the previously stored order response instead of creating another order.

Use a new key for a new checkout.

---

## 9. Order history

### Request

```http
GET /api/orders
Authorization: Basic <user-credentials>
Accept: application/json
```

### Success — 200

```json
[
  {
    "orderId": 1,
    "total": 1700.00,
    "status": "CREATED",
    "createdAt": "2026-09-20T15:30:00",
    "items": [
      {
        "bookId": 2,
        "title": "Effective Java",
        "quantity": 2,
        "unitPrice": 850.00,
        "subtotal": 1700.00
      }
    ]
  }
]
```

No orders:

```json
[]
```

---

## 10. Swagger / OpenAPI

Swagger UI:

```text
http://localhost:8081/swagger-ui.html
```

OpenAPI JSON:

```text
http://localhost:8081/v3/api-docs
```

---

## 11. H2 console

```text
http://localhost:8081/h2-console
```

Development connection:

```text
JDBC URL: jdbc:h2:mem:bookstore
User: sa
Password: <empty>
```

---

## Request ID

Clients may provide:

```http
X-Request-Id: TEST-12345
```

If omitted, the application generates a request ID. The request ID is returned in the response and is available in request logs.

## Status code summary

| Status | Meaning | Typical examples |
|---:|---|---|
| 200 | Success | GET books/cart/orders, update cart |
| 201 | Created | Register, add book, add cart item, checkout |
| 204 | No Content | Remove cart item |
| 400 | Bad Request | Validation, malformed JSON, empty cart, missing idempotency key |
| 401 | Unauthorized | Missing/invalid credentials |
| 403 | Forbidden | USER attempting ADMIN operation |
| 404 | Not Found | Missing book/cart item |
| 409 | Conflict | Duplicate ISBN, idempotency conflict, insufficient stock depending on business case |
| 500 | Internal Server Error | Unexpected server error |

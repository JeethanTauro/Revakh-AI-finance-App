# Revakh

A personal finance management platform built on a microservices architecture with Event-Driven Architecture (EDA) and an AI-powered chatbot assistant.

---

## Overview

Revakh helps users manage their personal finances — tracking transactions, setting category budgets, monitoring spending, and getting intelligent insights through a conversational AI chatbot. Services communicate asynchronously via rabbitmq, keeping the system resilient and scalable.

---

## Architecture

| Service | Language / Framework | Responsibility |
|---|---|---|
| Auth Service | Java / Spring Boot | User registration, login, JWT management |
| Finance Service | Java / Spring Boot | Wallet, transactions, categories, budgets |
| AI Chatbot Service | Python / FastAPI | Conversational AI for financial insights |

Inter-service communication is handled via **RabbitMQ** (topic exchange). The platform is fully containerised with **Docker** and run locally via **Docker Compose**.

---

## Tech Stack

- **Java / Spring Boot** — Auth & Finance Services
- **Python / FastAPI** — AI Chatbot Service
- **RabbitMQ** — Event-Driven messaging (topic exchange)
- **Docker & Docker Compose** — Containerisation & local orchestration

---

## Getting Started

### Prerequisites

- Docker & Docker Compose
- Java 17+
- Python 3.10+

### Run Locally

```bash
git clone https://github.com/your-org/revakh.git
cd revakh
docker-compose up --build
```

---

## API Gateway

The gateway is a **Spring Cloud Gateway** running on port `8083`. All client requests go through here — it validates JWTs, injects the `userId` header, and routes traffic to the appropriate downstream service.

### Routing Table

| Route | Downstream Service | Port |
|---|---|---|
| `/api/auth/**` | Auth Service | `8080` |
| `/api/finance/**` | Finance Service | `8081` |
| `/api/AI/**` | AI Chatbot Service | `8082` |

All routes pass through the `JwtFilter`. The filter validates the Bearer token, extracts the `userId` from the JWT claims, and injects it as a header — this is how every downstream service receives a trusted `userId` without trusting user-supplied input.

### Public Routes (No JWT required)

The following endpoints bypass the JWT filter:

```
/api/auth/register
/api/auth/register/verify
/api/auth/login
/api/auth/refresh-access
/api/auth/reset-password/**
```

Everything else requires a valid `Authorization: Bearer <token>` header.

### Environment Variables

| Variable | Default |
|---|---|
| `AUTH_SERVICE_URL` | `http://localhost:8080` |
| `FINANCE_SERVICE_URL` | `http://localhost:8081` |
| `AI_SERVICE_URL` | `http://localhost:8082` |

---

## Auth Service API

Base path: `/api/auth`

---

### Registration

Registration is a two-step process — submit details, then verify via OTP.

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/register` | Step 1 — Submit details, OTP sent to email |
| `POST` | `/register/verify` | Step 2 — Verify OTP to complete registration |

#### `POST /register` — Request Body
```json
{
  "userName": "john",
  "userEmail": "john@example.com",
  "userInternationalCode": "+91",
  "userNumber": 9876543210,
  "userBirthDate": "1998-05-15",
  "userPassword": "StrongPass123!"
}
```

#### `POST /register/verify` — Request Body
```json
{
  "userEmail": "john@example.com",
  "otp": "482910"
}
```

#### Auth Response (on successful verify / login)
```json
{
  "accessToken": "eyJhbGci...",
  "refreshToken": "eyJhbGci...",
  "message": "User registered successfully",
  "userId": 1
}
```

---

### Login & Token Management

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/login` | Authenticate and receive access + refresh tokens |
| `POST` | `/refresh-access` | Get a new access token using a refresh token |
| `DELETE` | `/user-delete` | Delete the authenticated user's account |

#### `POST /login` — Request Body
```json
{
  "userEmail": "john@example.com",
  "userPassword": "StrongPass123!"
}
```

#### `POST /refresh-access` — Request Body
```json
{
  "refresh": "eyJhbGci..."
}
```
> When the frontend receives a `401`, it sends the stored refresh token (from an HTTP-only cookie) to this endpoint to get a new access token.

---

### Password Reset (Unauthenticated)

For users who have forgotten their password. Three-step flow.

Base path: `/api/auth/reset-password`

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/generate` | Step 1 — Send OTP to registered email |
| `POST` | `/validate` | Step 2 — Verify OTP, receive a temporary reset token |
| `POST` | `/confirm` | Step 3 — Submit new password using reset token |

#### `POST /generate` — Request Body
```json
{
  "userEmail": "john@example.com"
}
```

#### `POST /validate` — Request Body
```json
{
  "userEmail": "john@example.com",
  "otp": "482910"
}
```

#### `POST /validate` — Response
```json
{
  "message": "OTP verified successfully. Use this token to reset your password.",
  "resetToken": "eyJhbGci..."
}
```

#### `POST /confirm` — Headers & Body
```
Authorization: Bearer <resetToken>
```
```json
{
  "newPassword": "MyNewStrongPass123!"
}
```

---

### Password Update (Authenticated)

Base path: `/api/auth/update-password`

| Method | Endpoint | Description |
|---|---|---|
| `PATCH` | `/` | Change password while logged in |

#### `PATCH /` — Request Body
```json
{
  "oldUserPassword": "OldPass123!",
  "newUserPassword": "NewPass456!"
}
```

---

### Email Update (Authenticated)

Base path: `/api/auth/update-email`

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/initiate` | Step 1 — Send OTP to the new email address |
| `POST` | `/verify` | Step 2 — Verify OTP to confirm new email |

#### `POST /initiate` — Request Body
```json
{
  "newEmail": "john_new@example.com",
  "currentPassword": "StrongPass123!"
}
```

#### `POST /verify` — Request Body
```json
{
  "userEmail": "john_new@example.com",
  "otp": "391847"
}
```
> Returns a fresh `AuthResponseDTO` with updated tokens on success.

---

## Finance Service API

Base path: `/api/finance/users`

> All endpoints require a `userId` header (injected by the Auth Service after token validation).

---

### Wallet

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/wallet` | Get current wallet balance and currency |
| `POST` | `/wallet/top-up` | Add funds to wallet |

#### `GET /wallet` — Response
```json
{
  "userId": 1,
  "balance": 5000.00,
  "currency": "INR"
}
```

#### `POST /wallet/top-up` — Request Body
```json
{
  "amount": 1000.00,
  "description": "Monthly savings"
}
```
> Internally creates a `CREDIT` transaction under the `TOP_UP` category. No need to pass category or type.

---

### Transactions

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/transaction-ledger` | Record a new transaction |
| `GET` | `/transaction-ledger/history` | List all transactions for user |
| `GET` | `/transaction-ledger/daily?targetDate=YYYY-MM-DD` | Transactions grouped by category for a given day |
| `GET` | `/transaction-ledger/{transactionId}` | Fetch a single transaction |
| `DELETE` | `/transaction-ledger/{transactionId}` | Soft-delete a transaction |

#### `POST /transaction-ledger` — Request Body
```json
{
  "amount": 250.00,
  "categoryName": "FOOD",
  "source": "UPI",
  "transactionType": "DEBIT",
  "description": "Lunch"
}
```
> `source` accepts: `MANUAL`, `UPI`, `NET_BANKING`  
> `transactionType` accepts: `CREDIT`, `DEBIT`

#### Transaction Response
```json
{
  "transactionId": 12,
  "userId": 1,
  "userName": "john",
  "amount": 250.00,
  "categoryName": "FOOD",
  "type": "DEBIT",
  "source": "UPI",
  "balance": 4750.00,
  "description": "Lunch",
  "date": "2025-06-01T13:45:00"
}
```

#### `GET /transaction-ledger/daily` — Response
```json
{
  "date": "2025-06-01",
  "totalExpense": 500.00,
  "totalIncome": 2000.00,
  "netBalance": 1500.00,
  "categories": {
    "FOOD": [
      {
        "transactionId": 12,
        "description": "Lunch",
        "amount": 250.00,
        "type": "EXPENSE"
      }
    ]
  }
}
```

> **Note:** Soft-deleting a transaction does not revert the wallet balance (by design, to preserve audit history).

---

### Categories

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/categories` | List all active categories (System + Custom) |
| `POST` | `/categories` | Create a custom category |
| `DELETE` | `/categories/{categoryId}` | Soft-delete a custom category |

#### `POST /categories` — Request Body
```json
{
  "categoryName": "SUBSCRIPTIONS",
  "type": "EXPENSE"
}
```
> `type` accepts: `EXPENSE`, `INCOME`  
> Income categories are restricted to `SALARY` and `TOP_UP` only.  
> A category cannot be deleted if it has existing transactions or active budgets.

#### Category Response
```json
{
  "categoryId": 5,
  "name": "SUBSCRIPTIONS",
  "categoryType": "EXPENSE",
  "isSystem": false,
  "isActive": true
}
```

---

### Budgets

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/budget` | Create a budget for a category |
| `PUT` | `/budget/{budgetId}` | Update budget limit or period |
| `DELETE` | `/budget/{budgetId}` | Soft-delete a budget |
| `GET` | `/budgets` | List all active budgets (no consumption data, faster) |
| `GET` | `/budget/consumption/{budgetId}` | Get spending breakdown for one budget |
| `GET` | `/budgets/detailed` | Full dashboard — all budgets with consumption data |
| `GET` | `/budgets/reset` | Manually reset expired budget periods (testing only) |

#### `POST /budget` — Request Body
```json
{
  "categoryId": 5,
  "limitAmount": 3000.00,
  "period": "MONTHLY"
}
```
> `period` accepts: `DAILY`, `WEEKLY`, `MONTHLY`, `YEARLY`  
> Limit cannot exceed wallet balance. Income-type categories cannot have budgets.

#### Budget Response
```json
{
  "budgetId": 3,
  "categoryId": 5,
  "categoryName": "FOOD",
  "limitAmount": 3000.00,
  "period": "MONTHLY",
  "periodStart": "2025-06-01",
  "periodEnd": "2025-06-30",
  "isActive": true
}
```

#### `GET /budget/consumption/{budgetId}` — Response
```json
{
  "budgetId": 3,
  "limitAmount": 3000.00,
  "spentAmount": 1800.00,
  "remainingAmount": 1200.00,
  "percentageUsed": 60,
  "overspent": 0.00,
  "isHalfReached": true,
  "isEightyReached": false,
  "isFullReached": false,
  "budgetExceeded": false
}
```

#### `GET /budgets/detailed` — Response
Returns the same fields as above merged per budget — ideal for rendering the full finance dashboard.

> **Period Update Behaviour:** If you change a budget's `period`, the old budget is archived and a new one is created automatically.

---

## AI Chatbot Service API

Base path: `/api/AI`

The AI service is a Python/FastAPI app that provides a **RAG (Retrieval-Augmented Generation)** chatbot personalised to each user's financial history. On startup, it launches a background RabbitMQ consumer that listens for finance events and builds the user's memory store automatically.

---

### How It Works

```
Finance Service ──(RabbitMQ)──► AI Consumer ──► Generate Narrative ──► Store in ChromaDB
                                                                               │
User ──► POST /api/AI/chat ──► Query ChromaDB (by userId) ──► Groq LLM ──► Response
```

1. **Event Ingestion** — The consumer subscribes to three routing keys on the `app.global.exchange` topic exchange: `finance.transaction.created`, `finance.budget.created`, and `finance.budget.updated`.
2. **Narrative Generation** — Each event is converted into a 3–4 sentence semantic narrative via the LLM before storage.
3. **Vector Storage** — Narratives are embedded and stored in **ChromaDB** (cosine similarity), tagged with `userId` for strict per-user isolation.
4. **Chat** — On each query, the top 5 most relevant narratives for that user are retrieved and passed as context to the LLM.

---

### Endpoints

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/` | Health check |
| `POST` | `/api/AI/chat` | Ask the AI a question about your finances |

#### `POST /api/AI/chat` — Headers
```
userId: 1
```

#### `POST /api/AI/chat` — Request Body
```json
{
  "query": "How much did I spend on food this month?"
}
```

#### `POST /api/AI/chat` — Response
```json
{
  "answer": "Based on your records, you have spent ₹1,800 on food this month across 4 transactions, leaving ₹1,200 remaining in your food budget."
}
```

---

### RabbitMQ Event Payloads

The AI consumer expects the following event structures published by the Finance Service.

#### `finance.transaction.created`
```json
{
  "eventId": "uuid",
  "userId": 1,
  "transactionId": "12",
  "amount": 250.00,
  "currency": "INR",
  "description": "Lunch",
  "category": "FOOD",
  "type": "DEBIT",
  "occurredAt": "2025-06-01T13:45:00"
}
```

#### `finance.budget.created` / `finance.budget.updated`
```json
{
  "eventId": "uuid",
  "userId": 1,
  "budgetId": 3,
  "limitAmount": 3000.00,
  "category": "FOOD",
  "period": "MONTHLY",
  "active": true,
  "createdAt": "2025-06-01T00:00:00"
}
```

---



| Code | Meaning |
|---|---|
| `400` | Validation error, insufficient balance, or business rule violation |
| `404` | User, category, or budget not found |
| `409` | Duplicate resource (e.g. budget or category already exists) |

---

## Project Structure

```
revakh/
├── api-gateway/               # Spring Cloud Gateway — JWT validation & routing
├── auth-service/              # Java Spring Boot — authentication & JWT
├── finance-service/           # Java Spring Boot — wallet, transactions, budgets
│   └── src/main/java/finance_service/revakh/
│       ├── controller/        # REST controllers
│       ├── service/           # Business logic
│       ├── models/            # JPA entities
│       └── DTO/               # Request & response DTOs
├── AI-service/                # Python FastAPI — AI chatbot
│   └── app/
│       ├── main.py            # FastAPI app + lifespan
│       ├── ai_service.py      # RAG logic (retrieval + generation)
│       ├── consumer.py        # RabbitMQ consumer
│       ├── database.py        # ChromaDB client + narrative generation
│       └── finance_memory/    # ChromaDB persistent vector store
└── docker-compose.yml
```


---

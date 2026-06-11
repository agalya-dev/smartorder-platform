# 🚀 SmartOrder Platform

> **Event-Driven Order & Payment System with Agentic AI**  
> Built with Java 17 · Spring Boot 3.2 · Couchbase · Apache Kafka · React · Claude AI

---

## 📋 Overview

SmartOrder is a production-grade, event-driven microservices platform for order and payment processing. It demonstrates enterprise patterns including ACID transactions, event sourcing, ERA rule engines, agentic AI integration, and role-based React UI.

Built as a portfolio project showcasing skills in:
- **Event-driven architecture** using Apache Kafka
- **ACID transactions** using Couchbase multi-document transactions
- **Agentic AI** using Anthropic Claude API
- **JWT authentication** with role-based access control
- **Real-time alerts** with ERA (Event-Rule-Action) engine

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                        React UI (Port 5173)                      │
│         Admin Dashboard │ Manager Dashboard │ User Dashboard      │
│              AI Chat Assistant │ Real-time Feed                   │
└──────────────────────────────┬──────────────────────────────────┘
                               │ REST APIs + JWT
┌──────────────────────────────▼──────────────────────────────────┐
│                   Spring Boot Backend (Port 8080)                 │
│                                                                   │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌────────────────┐  │
│  │  Order   │  │ Payment  │  │   Feed   │  │   AI Agents    │  │
│  │ Service  │  │ Service  │  │ Service  │  │  Claude API    │  │
│  └────┬─────┘  └────┬─────┘  └──────────┘  └────────────────┘  │
│       │              │                                            │
│  ┌────▼──────────────▼─────────────────────────────────────┐    │
│  │              CB ACID Transaction (All or Nothing)         │    │
│  │   ORDER + ERA + ALERT + AUDIT + STATUS atomically        │    │
│  └──────────────────────────┬────────────────────────────┘      │
│                              │                                    │
│  ┌───────────────────────────▼────────────────────────────────┐  │
│  │                    ECA Handler                              │  │
│  │  (Event-Condition-Action) processes in memory              │  │
│  └───────────────────────────┬────────────────────────────────┘  │
│                              │                                    │
│  ┌───────────────────────────▼────────────────────────────────┐  │
│  │                    ERA Rule Engine                          │  │
│  │  HighValueOrder │ BulkOrder │ PaymentFailed │ Cancelled    │  │
│  └───────────────────────────┬────────────────────────────────┘  │
└──────────────────────────────┼──────────────────────────────────┘
                               │ Kafka Events (Async)
┌──────────────────────────────▼──────────────────────────────────┐
│                      Apache Kafka (Port 9092)                     │
│  order.events │ payment.events │ feed.updates │ notifications     │
│  era.alerts   │ audit.logs                                        │
└──────────┬────────────────────────────────────────┬─────────────┘
           │                                        │
┌──────────▼──────────┐                 ┌──────────▼──────────────┐
│   ECA Kafka Consumer │                 │  Notification Consumer   │
│   → Feed updates     │                 │  → Email (Option A: Log) │
│   → AI enrichment    │                 │  → Admin alerts          │
└─────────────────────┘                 └─────────────────────────┘
```

---

## 🎯 Complete Order & Payment Lifecycle

```
POST /api/v1/orders
        │
        ▼
1. Validate (Custom validator — enterprise style)
2. Convert currency to SEK (Frankfurter API, daily update)
3. ECA processes in memory
4. ERA evaluates rules in memory
5. CB ACID Transaction (ALL or NOTHING):
   ├── ORDER::ORD-001          (status: PENDING_PAYMENT)
   ├── ERA::ORDER_CREATED::ORD-001
   ├── ALERT::ADMIN::ORDER::ORD-001  (if high value)
   ├── AUDIT::ORD-001::2026-06-11
   └── STATUS::ORD-001
6. Kafka publish: order.events (async)
7. Return response to client

        │ Client initiates payment
        ▼

POST /api/v1/payments
        │
        ├── Attempt 1 or 2 FAILED → RETRY (order stays PENDING)
        │
        └── Attempt 3 FAILED → CB Transaction:
               ├── PAYMENT::PAY-001    (FAILED)
               ├── ORDER::ORD-001      (CANCELLED ← auto!)
               ├── ERA::PAYMENT_FAILED::ORD-001
               ├── ALERT::ADMIN::PAYMENT::ORD-001 (CRITICAL)
               └── AUDIT::PAY-001
               └── Kafka → Notification → Email to user + admin

        │ Payment CONFIRMED
        ▼
CB Transaction:
├── PAYMENT::PAY-001    (CONFIRMED)
├── ORDER::ORD-001      (CONFIRMED ← auto!)
└── AUDIT::PAY-001
```

---

## 🤖 Agentic AI Features

| Agent | Endpoint | Description |
|-------|----------|-------------|
| **FraudDetector** | `POST /api/v1/ai/fraud-detect/{paymentId}` | Analyses payment for fraud risk using Claude AI |
| **ChatAssistant** | `POST /api/v1/ai/chat` | Conversational AI reads CB and answers questions |
| **RuleAdvisor** | `GET /api/v1/ai/explain/rules` | Explains ERA rules in plain English |
| **OrderExplainer** | `GET /api/v1/ai/explain/order/{orderId}` | Explains what happened to an order |

### Example AI Fraud Detection Response:
```json
{
  "paymentId": "PAY-3E1153FD",
  "riskLevel": "HIGH",
  "reason": "Amount SEK 50,000 is unusually high with 2 failed attempts",
  "recommendation": "Request additional 3D Secure verification"
}
```

### Example AI Chat Response:
```
Q: "What happened to order ORD-001?"
A: "Order ORD-001 was created on 11 June 2026 for SEK 2,400.
    Payment was attempted 3 times but failed due to insufficient funds.
    The order was automatically cancelled and an alert was sent to admin."
```

---

## 🏛️ ERA Rule Engine

| Rule | Trigger | Severity | Subscribers |
|------|---------|----------|-------------|
| `Rule::HighValueOrder` | Amount > SEK 10,000 | HIGH | ADMIN, MANAGER |
| `Rule::BulkOrder` | Items > 10 | MEDIUM | MANAGER |
| `Rule::PaymentFailed` | 3+ failed attempts | CRITICAL | ADMIN |
| `Rule::OrderCancelled` | Order cancelled | LOW | USER |

---

## 🗄️ Couchbase Document Design

```
smartorder-core (bucket)
├── EVENT::ORDER              ← description template
├── EVENT::PAYMENT            ← description template
├── Rule::HighValueOrder      ← ERA rule
├── Rule::BulkOrder           ← ERA rule
├── Rule::PaymentFailed       ← ERA rule
├── Rule::OrderCancelled      ← ERA rule
├── ORDER::ORD-001            ← order document
├── PAYMENT::PAY-001          ← payment document
├── ERA::ORDER_CREATED::ORD-001
├── STATUS::ORD-001
├── USER::USR-001             ← user profile
├── CONFIG::CURRENCY_RATES    ← daily exchange rates
└── IDEMPOTENCY::abc123       ← idempotency keys

smartorder-alerts (bucket)
├── ALERT::ADMIN::ORDER::ORD-001
├── ALERT::MANAGER::ORDER::ORD-001
└── ALERT::USER::PAYMENT::ORD-001

smartorder-audit (bucket)
└── AUDIT::ORD-001::2026-06-11
```

---

## 🔧 Tech Stack

| Layer | Technology |
|-------|-----------|
| **Backend** | Java 17, Spring Boot 3.2.5 |
| **Database** | Couchbase Community 7.1.1 |
| **Messaging** | Apache Kafka 3.7.0 (KRaft mode) |
| **AI** | Anthropic Claude API (claude-haiku) |
| **Security** | Spring Security + JWT (JJWT) |
| **Currency** | Frankfurter API (ECB data, daily update) |
| **Frontend** | React + Vite, Axios, React Router |
| **Monitoring** | Redpanda Console (Kafka UI) |
| **Container** | Docker |

---

## 🚀 Getting Started

### Prerequisites
- Java 17+
- Docker Desktop
- Node.js 20+
- Anthropic API key

### 1. Start Infrastructure

```bash
# Start Couchbase
docker run -d --name couchbase-smartorder \
  -p 8091-8096:8091-8096 -p 11210:11210 \
  couchbase:community-7.1.1

# Start Kafka
docker run -d --name kafka-smartorder -p 9092:9092 \
  -e KAFKA_NODE_ID=1 \
  -e KAFKA_PROCESS_ROLES=broker,controller \
  -e KAFKA_LISTENERS=PLAINTEXT://0.0.0.0:9092,CONTROLLER://0.0.0.0:9093 \
  -e KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://localhost:9092 \
  -e KAFKA_CONTROLLER_LISTENER_NAMES=CONTROLLER \
  -e KAFKA_LISTENER_SECURITY_PROTOCOL_MAP=CONTROLLER:PLAINTEXT,PLAINTEXT:PLAINTEXT \
  -e KAFKA_CONTROLLER_QUORUM_VOTERS=1@localhost:9093 \
  -e KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR=1 \
  -e CLUSTER_ID=MkU3OEVBNTcwNTJENDM2Qk \
  apache/kafka:3.7.0
```

### 2. Configure Couchbase

1. Open http://localhost:8091
2. Setup cluster with credentials: `Administrator / Admin@1234`
3. Create buckets: `smartorder-core`, `smartorder-alerts`, `smartorder-audit`
4. Create primary indexes:
```sql
CREATE PRIMARY INDEX ON `smartorder-core`;
CREATE PRIMARY INDEX ON `smartorder-alerts`;
CREATE PRIMARY INDEX ON `smartorder-audit`;
```

### 3. Configure Application

Create `src/main/resources/application-dev.properties`:
```properties
# Dev profile
logging.level.com.smartorder=INFO
anthropic.api.key=YOUR_CLAUDE_API_KEY
anthropic.api.url=https://api.anthropic.com/v1/messages
anthropic.model=claude-haiku-4-5
```

### 4. Run Backend

```bash
mvnw clean install -DskipTests
mvnw spring-boot:run
```

Backend runs on: http://localhost:8080

### 5. Run Frontend

```bash
cd smartorder-ui
npm install
npm run dev
```

Frontend runs on: http://localhost:5173

---

## 👤 Test Accounts

| Role | Email | Password |
|------|-------|----------|
| Admin | admin@smartorder.com | admin123 |
| Manager | manager@smartorder.com | manager123 |
| User | user@smartorder.com | user123 |

---

## 📡 API Endpoints

### Orders
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/orders` | Create order (any currency) |
| GET | `/api/v1/orders/{orderId}` | Get order |
| DELETE | `/api/v1/orders/{orderId}` | Cancel order |

### Payments
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/payments` | Initiate payment |
| PUT | `/api/v1/payments/{id}/confirm` | Confirm payment |
| PUT | `/api/v1/payments/{id}/fail` | Fail payment |
| GET | `/api/v1/payments/{id}` | Get payment |

### Feed
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/feed` | All events (Admin) |
| GET | `/api/v1/feed/user/{userId}` | User events |

### Alerts
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/alerts/admin` | Admin alerts |
| GET | `/api/v1/alerts/manager` | Manager alerts |
| PUT | `/api/v1/alerts/{key}/seen` | Mark seen |
| PUT | `/api/v1/alerts/{key}/resolve` | Resolve alert |

### AI Agents
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/ai/fraud-detect/{paymentId}` | Fraud detection |
| POST | `/api/v1/ai/chat` | AI chat assistant |
| GET | `/api/v1/ai/explain/rules` | Explain ERA rules |
| GET | `/api/v1/ai/explain/order/{orderId}` | Explain order |

### Users
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/users/register` | Register user |
| POST | `/api/v1/users/login` | Login (returns JWT) |
| POST | `/api/v1/users/logout/{userId}` | Logout |

---

## 🔐 Security

- JWT tokens (24 hour expiry)
- Role-based access: ADMIN, MANAGER, USER
- Idempotency keys prevent duplicate orders
- API key stored in local properties (never in Git)

---

## 💱 Currency Conversion

- Supports any currency (SEK, USD, EUR, GBP, INR, JPY...)
- Rates fetched daily from Frankfurter API (ECB data)
- Stored in Couchbase: `CONFIG::CURRENCY_RATES`
- All amounts converted to SEK for ERA rule evaluation

---

## 🌐 Frontend

Repository: [smartorder-ui](https://github.com/agalya-dev/smartorder-ui)

| Dashboard | Features |
|-----------|---------|
| **Admin** | All orders, feed with filter/sort/group, alerts with detail, audit trail, AI chat |
| **Manager** | Orders, feed, manager alerts |
| **User** | Place order, make payment, my feed, my orders |

---

## 👩‍💻 Author

**Agalya Pachaikannu**  
Senior Java Backend Engineer  
📍 Stockholm, Sweden  
🔗 [linkedin.com/in/agalya-pachaikannu-053641172](https://linkedin.com/in/agalya-pachaikannu-053641172)

---

## 📝 Notes

- Currency rates updated daily at midnight via scheduled job
- Notification service logs emails (Option A) — JavaMailSender integration planned (Day 12)
- WebSocket for real-time feed updates planned
- Docker Compose for one-command startup planned

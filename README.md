# 🛡️ OpsGuardian: AI-Powered DevOps Copilot

OpsGuardian is an **AI-powered DevOps Copilot** that understands natural language commands, makes intelligent decisions, executes safe actions, and explains its reasoning—all while strictly enforcing **security policies and permissions**.

Think of it as **ChatGPT for DevOps**, but with enterprise-grade guardrails.

---

## 🧠 Project Goal

OpsGuardian bridges the gap between AI autonomy and infrastructure security by enabling:

- **Natural Language Execution:** Run complex DevOps tasks via simple chat.
- **Intent Understanding:** LLM-driven reasoning to determine the best course of action.
- **Permission Enforcement:** Native integration with **Auth0 (JWT + RBAC)**.
- **Safe Guardrails:** Policy checks and mandatory confirmation steps for "destructive" actions.
- **Observability:** Real-time **technical flow visualization** in the UI.

---

## 📺 Demo

![OpsGuardian Demo](static/demo.gif)

**Example Flow:**
1. **Input:** User types `"Restart payment service"`.
2. **Reasoning:** AI displays its thought process (e.g., "Checking service status...").
3. **Safety Check:** A confirmation button appears because the action is flagged as "Risky."
4. **Validation:** System validates the user's **Auth0 JWT** for the required permissions.
5. **Execution:** Once confirmed, the service restarts and the UI flow updates in real-time.

---

## 🏗️ Architecture

### High-Level Component Map
```text
           ┌─────────────┐
           │  Frontend   │
           │ (JS Chat UI)│
           └─────┬───────┘
                 │
                 ▼
          ┌─────────────┐
          │ Spring Boot │
          │   API       │
          └─────┬───────┘
                 │
      ┌──────────┴──────────┐
      │  Auth0 JWT Validation│
      │  (issuer + audience) │
      └──────────┬──────────┘
                 │
          ┌─────────────┐
          │    LLM      │
          │ Decision Engine│
          └─────┬───────┘
                 │
     ┌───────────┴───────────┐
     │   Agent Service        │
     │  - Policy Check        │
     │  - RBAC Enforcement    │
     │  - Confirmation Layer  │
     └───────────┬───────────┘
                 │
      ┌──────────┴───────────┐
      │ External APIs        │
      │ - GitHub             │
      │ - AWS                │
      │ - Logs / Metrics     │
      └──────────┬───────────┘
                 │
                 ▼
              ┌───────┐
              │  UI   │
              │ Response │
              └───────┘

Logic Flow (Mermaid)

flowchart LR
    A[🧑 User Chat UI] -->|Natural Language Command| B[🤖 AI Layer (LLM)]
    B --> C[🧠 Agent Layer]
    C --> C1[Planner]
    C --> C2[Executor]
    C1 -->|Task Plan| C2
    C2 -->|Tool Selection| D[🧰 Tool Registry]
    D --> E1[GitHub Tool]
    D --> E2[Logs Tool]
    D --> E3[DevOps Tool]
    C2 --> F[🛡️ Policy Engine]
    F -->|Allow / Warn / Confirm| G[🔐 Auth0 RBAC]
    G --> H[⚙️ Execution Layer]
    H --> E1
    H --> E2
    H --> E3
    H --> I[📊 Response + Status]
    I --> J[💬 Chat UI + Flow Visualization]

✅ Features
1. LLM-Based Decision Making

Converts ambiguous commands into structured, actionable JSON payloads.

{
  "action": "RESTART_SERVICE",
  "target": "payment-service",
  "reason": "Service instability detected"
}

2. Secure Execution & Auth0

JWT Validation: Validates issuer and audience on every request.

RBAC: Maps Auth0 roles to specific DevOps actions (e.g., admin can restart, viewer only sees logs).

3. Safety Guardrails

High-risk actions like RESTART_SERVICE or SCALE_SERVICE trigger a Confirmation Layer, requiring a physical click from an authorized user before the backend proceeds.

4. Transparent Reasoning

The UI provides a live "Internal Thought" feed:

🧠 Understanding request...

🛡️ Checking permissions...

⚠️ Confirmation required

🚀 Executing...

✅ Completed!

🛠️ Setup Instructions
Backend (Java / Spring Boot)

Clone the repo:
git clone <repo-url>
cd copilot_backend

Configure Auth0 in src/main/resources/application.yml:

spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: <YOUR_AUTH0_DOMAIN>

Run the App:
./mvnw spring-boot:run

Frontend

The UI is served statically by Spring Boot at http://localhost:8080.

Ensure app.js contains a valid JWT for local testing.

⚙️ Available Commands
"Restart payment service" → RESTART_SERVICE

"Scale checkout service to 3 instances" → SCALE_SERVICE

"Show logs for orders service" → FETCH_LOGS

"Create GitHub issue in checkout-service" → CREATE_GITHUB_ISSUE

📝 Roadmap
[ ] Streaming Responses: Real-time AI typing effect via SSE.

[ ] Session Memory: Context-aware follow-up commands.

[ ] Cloud Native: Deep integration with AWS CloudWatch and Lambda.

[ ] Enhanced UI: Dark mode and persistent chat history.

📦 License
Distributed under the MIT License. See LICENSE for more information.

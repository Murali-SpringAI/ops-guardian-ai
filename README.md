OpsGuardian is an **AI-powered DevOps Copilot** that understands natural language commands, makes intelligent decisions, executes safe actions, and explains itself—all while enforcing **security policies and permissions**.

Think of it as a **ChatGPT for DevOps** with guardrails.

---

## 🧠 Project Goal

OpsGuardian enables:

- Natural language command execution
- LLM-based intent understanding
- Permission enforcement via **Auth0 (JWT + RBAC)**
- Safe execution with **policy checks and confirmation steps**
- Integration with external APIs (GitHub, AWS, logs, metrics)
- Real-time **technical flow visualization** in the UI

---

Watch OpsGuardian in action:

![OpsGuardian Demo](static/demo.gif)

**Flow showcased:**
1. User enters command → `"Restart payment service"`
2. AI reasoning steps appear in chat
3. Confirmation button appears for risky action
4. User confirms action
5. Execution result displayed
6. Technical flow visualization updates in real-time

## 🏗️ Architecture
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

---

<pre> ```mermaid flowchart LR A[🧑 User Chat UI] -->|Natural Language Command| B[🤖 AI Layer (LLM)] B --> C[🧠 Agent Layer] C --> C1[Planner] C --> C2[Executor] C1 -->|Task Plan| C2 C2 -->|Tool Selection| D[🧰 Tool Registry] D --> E1[GitHub Tool] D --> E2[Logs Tool] D --> E3[DevOps Tool] C2 --> F[🛡️ Policy Engine] F -->|Allow / Warn / Confirm| G[🔐 Auth0 RBAC] G --> H[⚙️ Execution Layer] H --> E1 H --> E2 H --> E3 H --> I[📊 Response + Status] I --> J[💬 Chat UI + Flow Visualization] ``` </pre>


## ✅ Features Implemented

### 1. LLM-Based Decision Making
- Converts user commands like `"Restart payment service"` into structured actions.
- Example decision:

```json
{
  "action": "RESTART_SERVICE",
  "target": "payment-service",
  "reason": "Service instability detected"
}
2. Secure Execution
Auth0 JWT validation (issuer + audience)
Role-based permissions for actions (restart, scale, view logs, GitHub operations)
Policy enforcement to prevent dangerous operations (e.g., restarting production)
3. Safety Guardrails
Confirmation required for high-risk actions:
RESTART_SERVICE
SCALE_SERVICE
Example flow:
LLM → Risk Check → Ask User Confirmation → Execute
4. Frontend Chat-Style UI
Input natural language commands
Real-time technical flow visualization
Chat bubbles for user vs AI messages
Confirmation button for risky actions
5. Integrations
GitHub: create issues, list repositories
Simulated DevOps APIs: restart/scale services, fetch logs, fetch metrics
Policy enforcement to simulate restricted AI mode
6. AI Reasoning & Flow
The UI displays step-by-step reasoning:
🧠 Understanding request...
📊 Analyzing intent...
⚙️ Deciding action...
🔐 Validating JWT with Auth0...
🛡️ Checking permissions...
⚠️ Confirmation required (if needed)
🚀 Executing...
✅ Completed!
⚡ Demo Script
Enter command: "Restart payment service"
Observe AI reasoning and technical flow
Confirm action if required
Execution result displayed with reasoning
Repeat with other commands (GitHub issues, logs, metrics)
🛠️ Setup Instructions
Backend
Clone repository:
git clone <repo-url>
cd copilot_backend
Configure Auth0 in application.yml:
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: <AUTH0_ISSUER_URI>
Build and run:
./mvnw spring-boot:run
Frontend
Located in src/main/resources/static
Open index.html in browser (served by Spring Boot)
Ensure app.js has a valid JWT token for testing
⚙️ Available Commands (Examples)
"Restart payment service" → RESTART_SERVICE
"Scale checkout service to 3 instances" → SCALE_SERVICE
"Show logs for orders service" → FETCH_LOGS
"Create GitHub issue in checkout-service" → CREATE_GITHUB_ISSUE
🛡️ Policies & Guardrails
No production restarts: blocked by policy
Scaling: requires explicit user confirmation
RBAC enforced: actions blocked if JWT lacks permission
📝 Next Steps / Roadmap
Streaming backend responses for real-time AI typing effect
Session memory to allow context-aware commands
Additional integrations: AWS, metrics dashboards, logs viewer
Soft policy warnings vs hard blocks for restricted AI mode
Enhanced UI: chat bubbles with timestamps, colors, and message history
👀 Hackathon Highlights
AI agent acts as intermediary for restricted AI (OpenClaw scenario)
Fully respects policies and permissions
Provides transparent reasoning and flow
Extensible to multiple DevOps integrations
🎯 End Goal
A production-like AI DevOps agent that:
Understands natural language
Makes decisions securely
Acts safely on external services
Explains its reasoning
Enforces policies
📂 File Structure (Relevant)
copilot_backend/
├─ src/main/java/com/opsguardian/copilot_backend/
│   ├─ service/AgentService.java
│   ├─ llm/LlmService.java
│   ├─ model/AgentDecision.java
│   ├─ model/AgentResponse.java
├─ src/main/resources/static/
│   ├─ index.html
│   ├─ app.js
📦 License
MIT License
# CodeColosseum - 1v1 Competitive Coding Platform

Welcome to **CodeColosseum**, a web-based, real-time 1v1 competitive programming platform. It allows developers and students to match based on their skill level, receive identical programming tasks, write and submit code, and see live results evaluated in a sandboxed environment.

This project was built as a term project for **CSE3044 - Software Engineering Course** (Spring 2025–2026, Marmara University).

---

##  Features

*   **Real-time 1v1 Matchmaking:** Players join a matchmaking queue and are dynamically paired against opponents with similar Elo ratings using an expanding search tolerance.
*   **Live Match Session Management:** Both matched players receive the same programming problem at the same instant with a synchronized countdown timer.
*   **Sandboxed Code Evaluation:** User submissions are evaluated securely inside isolated sandbox containers via the **Judge0 REST API**.
*   **Dynamic Elo Rating System:** Post-match ratings are computed automatically using standard Elo formulas with a configurable K-factor ($K=32$) and a rating floor of $0$.
*   **Problem Proposal & Review Workflow:** Contestants can propose custom programming tasks. Administrators can review, edit, approve, or reject submissions.
*   **Global Leaderboard & Match History:** A public global leaderboard ranking users by Elo, alongside private, detailed per-user historical match records.

---

## Architecture Overview

CodeColosseum utilizes a **two-tier client-server architecture** with a strict layered design in the backend:

1.  **Frontend (Client Tier):** React 18 Single-Page Application (SPA) bundled with Vite. Communicates with the backend via a REST API (JSON) and STOMP over WebSockets for real-time bidirectional push messages.
2.  **Backend (Server Tier):** Spring Boot 3.x / Java 17 application utilizing Spring Security (stateless JWT authentication), Spring WebSocket (STOMP), and Spring Data JDBC (`JdbcTemplate`).
3.  **Database:** Neon PostgreSQL 14+ database storing accounts, matches, submissions, problems, and Elo history.
4.  **Sandbox Executor:** Judge0 REST API managing compilation and sandboxed execution of user-submitted code in isolation.

For a detailed view of the architectural design, sequence diagrams, and class hierarchies, refer to [CodeColosseum_DSD.pdf](file:///CodeColosseum/docs/project%20docs/CodeColosseum_DSD.pdf).

---

## Repository Structure

```
CodeColosseum/
├── Backend/                 # Spring Boot Java Application
│   ├── src/
│   │   ├── main/            # Source code (controllers, services, repositories)
│   │   └── test/            # JUnit 5 & Mockito test suites
│   ├── pom.xml              # Maven dependency configuration
│   └── mvnw                 # Maven wrapper script
├── frontend/                # React 18 Client Application
│   ├── src/
│   │   ├── pages/           # Dashboard, Match, Leaderboard, Admin pages
│   │   ├── services/        # REST client & WebSocket helper hooks
│   │   └── main.jsx         # React application entry point
│   ├── package.json         # Node.js dependencies and scripts
│   └── vite.config.js       # Vite build configurations
└── docs/                    # Requirements, design, testing, & decision docs
    ├── project docs/        # SRS, DSD, and Term Proposal PDFs
    └── testing/             # CodeColosseum_TestingDocument.md
```

---

## Installation & Getting Started

### Prerequisites
*   **Java Development Kit (JDK) 17** or **21**
*   **Node.js** (v18 or higher) & **npm**
*   **PostgreSQL 14+** running locally or accessible via network

---

### Backend Setup

1.  **Configure Database:**
    Create a database named `codecolosseum` (or configure your parameters). Update your database connection settings in the `Backend/src/main/resources/application.properties` (or environment variables):
    ```properties
    spring.datasource.url=jdbc:postgresql://localhost:5408/codecolosseum
    spring.datasource.username=postgres
    spring.datasource.password=yourpassword
    ```

2.  **Ensure Judge0 API is Running:**
    By default, the backend expects a Judge0 API instance running at `http://localhost:2358`. You can run it via Docker or update the Judge0 endpoint inside `application.properties`.

3.  **Run the Backend:**
    Navigate to the backend directory and start the Spring Boot application using Maven:
    ```bash
    cd Backend
    ./mvnw spring-boot:run
    ```

---

### Frontend Setup

1.  **Install Node Modules:**
    Navigate to the frontend directory and install dependencies:
    ```bash
    cd frontend
    npm install
    ```

2.  **Configure Environment Variables:**
    Ensure `.env` contains the correct API endpoint configurations:
    ```env
    VITE_API_URL=http://localhost:8080
    ```

3.  **Run the Frontend Dev Server:**
    Start the Vite server:
    ```bash
    npm run dev
    ```
    The client application will be available at `http://localhost:5173`.

---

## Verification & Testing

The backend includes a comprehensive test suite (unit and integration controller slice tests) ensuring all mathematical invariants (Elo ratings, matchmaking tolerances, Judge0 verdict mappings) behave correctly.

To run the backend test suite:
```bash
cd Backend
./mvnw test
```

For a detailed breakdown of test plan, test cases, and coverage results, see the official [CodeColosseum_TestingDocument.md](file:///CodeColosseum/docs/project%20docs/CodeColosseum_TestingDocument.md).

---

## Design & Engineering Decisions

All structural decisions regarding requirements analysis, database schema choices, technology stack selection, and verification methodologies have been documented and justified in accordance with software engineering guidelines.

Please review the unified log: **[CodeColosseum_Decision_Log.md](file:///CodeColosseum/docs/project%20docs/CodeColosseum_Decision_Log.md)**.
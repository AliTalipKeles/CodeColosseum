# CodeColosseum - Consolidated Decision Log

This document consolidates the key engineering decisions made across all phases of the **CodeColosseum** project: Requirements Analysis (SRS), Software Design (DSD), and Verification (Testing).

---

## Requirements Phase Decisions (SRS)

### Decision 1: Code Execution Strategy
*   **Decision:** Delegate all user code execution to the external **Judge0 REST API** in the initial implementation.
*   **Alternatives Considered:** 
    1. Build a custom Docker-based execution sandbox from scratch.
    2. Use the third-party Judge0 API (Selected).
*   **Rationale:** Building a custom sandbox with adequate security, multi-language support, and resource limiting would consume a disproportionate share of the available development time within the one-semester limit. Judge0 provides a production-grade, well-tested sandbox with a straightforward REST interface.
*   **Trade-offs:** Introduces an external runtime dependency. System availability for match functionality is partially contingent on Judge0 uptime and rate limits.
*   **Risk Mitigation:** The execution logic is fully encapsulated behind the `JudgeService` interface (NFR-11). If Judge0 becomes unavailable or its terms change, only one implementation class (`Judge0ServiceImpl`) needs to be modified.
*   **Technical Debt:** A custom Docker-based runner remains a candidate for implementation in a subsequent iteration if the platform grows.

---

### Decision 2: Rating Algorithm Selection
*   **Decision:** Use the standard **Elo algorithm with a fixed K-factor of 32** for all rating updates.
*   **Alternatives Considered:** 
    1. Simple win/loss points system (e.g., +10 / -10 per match).
    2. Glicko-2 rating system.
    3. Standard Elo with K=32 (Selected).
*   **Rationale:** A simple points system does not account for opponent strength, making matches unfair. Glicko-2 adds rating deviation and volatility parameters which improve accuracy but significantly increase implementation complexity. Standard Elo is well-understood, widely used, straightforward to implement, and produces interpretable ratings.
*   **Trade-offs:** K=32 can produce volatile rating swings for new users with few matches. It does not account for rating reliability (unlike Glicko-2).
*   **Risk Mitigation:** The rating logic is isolated in a single `RatingService` class, and the K-factor is configured as an adjustable parameter rather than a hard-coded constant.
*   **Technical Debt:** Migrating to Glicko-2 is documented as a future improvement.

---

### Decision 3: Match Winner Determination
*   **Decision:** If no contestant achieves an Accepted (AC) verdict before the timer expires, the winner is determined by the **number of test cases passed** across all submissions. If they are equal, the match is a draw.
*   **Alternatives Considered:** 
    1. Always draw if no AC.
    2. Partial scoring based on test cases passed (Selected).
    3. Penalty system (like LeetCode/Codeforces).
*   **Rationale:** A pure "first AC wins or draw" rule penalises contestants who make substantial partial progress but are narrowly beaten or encounter a difficult edge case. Partial test case scoring rewards productive effort and reduces the frequency of uninformative draws.
*   **Trade-offs:** Requires tracking and comparing per-submission test case pass counts, adding modest complexity to match resolution. Wrong Answers are also not penalised, which slightly reduces rating accuracy.
*   **Risk Mitigation:** The resolution logic is isolated entirely within `MatchService` and does not affect other components.
*   **Technical Debt:** None identified.

---

## Design Phase Decisions (DSD)

### Decision 4: Persistence Strategy
*   **Decision:** Avoid heavyweight ORM frameworks (JPA/Hibernate) and utilize Spring's **JdbcTemplate** for direct SQL execution.
*   **Alternatives Considered:** 
    1. Spring Data JPA / Hibernate.
    2. Raw JDBC / Spring `JdbcTemplate` (Selected).
*   **Rationale:** To ensure maximum transparency and performance, the team opted against ORM abstractions. Direct SQL allows for precise optimization of complex queries (e.g., matchmaking joins excluding already-seen problems) and avoids "hidden" lazy-loading and query overhead. It also ensures developers maintain a clear understanding of transaction boundaries.
*   **Trade-offs:** Increases the volume of boilerplate SQL code. Changes to the schema require manual updates to SQL strings in the Repository layer.
*   **Risk Mitigation:** The Repository layer is strictly isolated. All SQL is centralized within these classes to ensure that schema changes do not leak into the Service or Controller layers.
*   **Technical Debt:** Higher maintenance effort for the Repository layer if the domain model grows.

---

### Decision 5: Real-time Communication Protocol
*   **Decision:** Use **STOMP over WebSockets** for real-time match events.
*   **Alternatives Considered:** 
    1. HTTP Long Polling.
    2. Server-Sent Events (SSE).
    3. WebSockets with STOMP sub-protocol (Selected).
*   **Rationale:** Real-time matches require bidirectional, low-latency communication (e.g. timers, opponent submission alerts, immediate verdict delivery). Long polling is too resource-heavy. While SSE is lightweight, STOMP over WebSockets provides a robust pub/sub model for topic subscriptions, making it easy to isolate private contestant verdicts.
*   **Trade-offs:** WebSockets maintain open TCP connections, increasing server memory. Requires specialized handling for connection drops and reconnects during matches.
*   **Risk Mitigation:** Heartbeats are configured to detect stale connections, and a 5-minute grace period/re-entry window is implemented for disconnected players (FR-4.4).
*   **Technical Debt:** None; standard practice for real-time competitive platforms.

---

### Decision 6: Authentication Mechanism
*   **Decision:** Use stateless **JSON Web Tokens (JWT) using HMAC-SHA256** carried in the HTTP `Authorization: Bearer` header.
*   **Alternatives Considered:** 
    1. Server-side HTTP sessions.
    2. OAuth 2.0 via external providers.
    3. Stateless JWT (Selected).
*   **Rationale:** Server-side sessions require sticky load balancing or shared session stores (unjustified overhead at our scale). OAuth 2.0 introduces external dependency chains. JWTs are self-contained, validate without database round-trips, attach cleanly to both REST and WebSocket upgrade handshakes, and are natively supported by Spring Security.
*   **Trade-offs:** Tokens cannot be revoked before expiry without a blocklist. Client-side storage has a known XSS/CSRF trade-off.
*   **Risk Mitigation:** Short token lifetime (24 hours), externalised signing secret, and reuse of the same JWT validator for WebSocket upgrade handshakes.
*   **Technical Debt:** A Redis-backed blocklist and refresh-token rotation are candidate improvements.

---

### Decision 7: Matchmaking Queue Implementation
*   **Decision:** Implement the matchmaking queue as an **in-memory ConcurrentLinkedQueue** scanned every 1000 ms by a `@Scheduled` task.
*   **Alternatives Considered:** 
    1. PostgreSQL queue with `SELECT ... FOR UPDATE SKIP LOCKED`.
    2. Redis sorted set.
    3. In-memory queue (Selected).
*   **Rationale:** A DB-backed queue couples an ephemeral concern to a durable store and generates write load on every scan. Redis would scale horizontally but adds a second infrastructure component unjustified at tens of concurrent users. The in-memory queue meets the functional requirement with zero extra dependencies and scans in under 1 ms per cycle at the target load.
*   **Trade-offs:** Queue state is volatile (lost on server restart) and not replicable across multiple server nodes.
*   **Risk Mitigation:** All queue access is encapsulated in `MatchmakingService`; a future migration to Redis requires changes in only one place. The frontend treats queue state as transient, so restart loss is visible but not destructive.
*   **Technical Debt:** Redis-backed queue is required for multi-instance deployment.

---

### Decision 8: Matchmaking Proximity Threshold & Scan Interval
*   **Decision:** Pair contestants within a **fixed ±200 Elo rating threshold**, scanning the queue every **1000 ms**.
*   **Alternatives Considered:** 
    1. Stricter ±100 threshold.
    2. Dynamic threshold widening based on wait time.
    3. Event-driven matchmaking on enqueue.
    4. Fixed ±200 threshold with 1000 ms scan (Selected).
*   **Rationale:** A tighter threshold risks indefinite wait times in low-concurrency academic labs. Dynamic widening is fairer under load but requires complex per-entry timer state. Event-driven matching creates race conditions during concurrent queueing. A periodic scan linearises pairing decisions simply.
*   **Trade-offs:** At low population, some contestants may wait indefinitely with no escalation. The 1000 ms scheduler imposes a floor on pairing latency.
*   **Risk Mitigation:** Both parameters are externalised in configuration (`matchmaking.elo-threshold`, `matchmaking.scan-interval-ms`) allowing tuning without recompilation.
*   **Technical Debt:** Implementing dynamic-widening threshold at higher user volumes.

---

### Decision 9: Frontend Framework
*   **Decision:** Build the client-side user interface as a **React 18 Single-Page Application (SPA)** bundled with **Vite**.
*   **Alternatives Considered:** 
    1. Vue 3.
    2. Angular 17.
    3. Svelte.
    4. Server-rendered Thymeleaf.
    5. React 18 (Selected).
*   **Rationale:** Server-rendered templates are unsuitable for highly interactive elements (live timers, WebSocket-driven state updates). Among SPAs, React was chosen due to team familiarity, Vite's simple and fast compilation pipeline, and the availability of mature libraries like `@stomp/stompjs` and `@monaco-editor/react`.
*   **Trade-offs:** React is unopinionated about state management and folder structure, requiring additional team alignment on project structure.
*   **Risk Mitigation:** State was kept simple (`useState` + `useContext`, no complex external store), and WebSocket logic was centralised in a reusable `useMatchSocket` hook.
*   **Technical Debt:** Migration to TypeScript is recommended as a next step.

---

## Verification Phase Decisions (Testing)

### Decision 10: Mockito Repository Isolation
*   **Decision:** Mock repository interfaces using **Mockito** in unit tests instead of using a live database.
*   **Alternatives Considered:** 
    1. H2 in-memory database.
    2. Testcontainers with a real PostgreSQL instance.
    3. Mockito mocks (Selected).
*   **Rationale:** The repository layer uses PostgreSQL-specific SQL syntax (e.g. UUID casting). H2 would reject these queries without significant shims. Testcontainers would require Docker and a CI-ready environment neither of which is guaranteed in the course lab. Mocking the repositories is sufficient to test the service logic in isolation.
*   **Trade-offs:** SQL query syntax correctness is not verified by automated tests. A repository bug (wrong column name, syntax error) would not be caught at unit level.
*   **Risk Mitigation:** Divergence risks are minimized by isolating queries to repositories and verifying SQL manually during development.
*   **Technical Debt:** Integration tests with a real database context using Testcontainers.

---

### Decision 11: WebSocket Handler Exclusion
*   **Decision:** Exclude WebSocket handlers (`MatchHandler`, `MatchmakingHandler`) from the automated test suite.
*   **Alternatives Considered:** 
    1. Integration testing using `WebSocketStompClient`.
    2. Unit testing with a custom handler adapter.
    3. Exclude from automated suite, cover via manual demo (Selected).
*   **Rationale:** Testing WebSockets requires significant setup for handshakes, session lifecycles, and timing of `@Scheduled` tasks. The complexity-to-benefit ratio is too high within the one-semester timeline. Manual verification covers these flows adequately.
*   **Trade-offs:** Concurrency bugs (race conditions, queue re-insertion order) remain undetected by automated tests.
*   **Risk Mitigation:** A 10-second grace period is implemented on match creation to absorb connection drops, and scheduler operations are synchronized.
*   **Technical Debt:** Concurrency and load testing for real-time matchmaking.

---

### Decision 12: MockMvc Standalone Setup
*   **Decision:** Use MockMvc's **`standaloneSetup`** in `ProblemControllerTest` instead of the `@WebMvcTest` slice annotation.
*   **Alternatives Considered:** 
    1. `@WebMvcTest(ProblemController.class)` with `@MockBean`.
    2. `@SpringBootTest` with `@AutoConfigureMockMvc`.
    3. `standaloneSetup` (Selected).
*   **Rationale:** `@WebMvcTest` requires autoconfiguration classes not bundled with the `spring-boot-starter-webmvc-test` artifact. `@SpringBootTest` would attempt to load the full context, requiring a live database connection. `standaloneSetup` loads only the controller under test and exercises the request-dispatch path without external resources.
*   **Trade-offs:** Bypasses Spring's `DispatcherServlet` exception resolvers; unhandled exceptions would not be translated. For this controller, all errors are explicitly handled with `ResponseEntity` returns.
*   **Risk Mitigation:** Controller endpoints explicitly catch and map failures to proper `ResponseEntity` objects.
*   **Technical Debt:** Integrating global exception handlers (`@ControllerAdvice`) in tests if they are introduced in the future.

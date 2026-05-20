package com.example.demo.controllers.WebSocket;

import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import com.example.demo.dtos.GetProblemDto;
import com.example.demo.dtos.MatchDto;
import com.example.demo.dtos.TestCaseDto;
import com.example.demo.repositories.MatchRepository;
import com.example.demo.repositories.ProblemRepository;
import com.example.demo.repositories.UserRepository;
import com.example.demo.services.RatingService;
import com.example.demo.services.WebSocket.MatchService;
import com.example.demo.util.JwtUtil;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Component
public class MatchHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ConcurrentHashMap<String, WebSocketSession> pendingSessions = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, WebSocketSession> authenticatedSessions = new ConcurrentHashMap<>();
    private final MatchService matchService;
    private final MatchRepository matchRepository;
    private final ProblemRepository problemRepository;
    private final RatingService ratingService;

    private final Map<String, Integer> LanguageMap = Map.of("JAVA", 62, "PYTHON", 71);
    List<String> validStatuses = List.of(
            "ACCEPTED",
            "WRONG_ANSWER",
            "TIME_LIMIT",
            "MEMORY_LIMIT",
            "RUNTIME_ERROR",
            "COMPILATION_ERROR");

    public MatchHandler(MatchService matchService, MatchRepository matchRepository, ProblemRepository problemRepository,
            UserRepository userRepository, RatingService ratingService) {
        this.matchService = matchService;
        this.problemRepository = problemRepository;
        this.ratingService = ratingService;
        this.matchRepository = matchRepository;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        session.getAttributes().put("connectedAt", Instant.now());
        pendingSessions.put(session.getId(), session);
        System.out.println("[CONNECTION] New connection: sessionId=" + session.getId());
        System.out.println("[CONNECTION] Pending sessions: " + pendingSessions.size());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        System.out.println("[MESSAGE] sessionId=" + session.getId() + " → " + message.getPayload());

        @SuppressWarnings("unchecked")
        Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);
        String type = (String) payload.get("type");

        if ("auth".equals(type)) {
            System.out.println("[MESSAGE] Auth message received, processing...");
            handleAuth(session, payload);
            return;
        }

        if (session.getAttributes().get("userId") == null) {
            System.out.println("[MESSAGE] Message received without auth, closing: sessionId=" + session.getId());
            session.close(CloseStatus.POLICY_VIOLATION);
            return;
        }

        handleGameMessage(session, payload);
    }

    private void handleAuth(WebSocketSession session, Map<String, Object> payload) throws Exception {
        String token = (String) payload.get("token");
        String matchId = (String) payload.get("matchId");

        if (token == null || matchId == null) {
            System.out.println("[AUTH] Missing fields: token=" + token + " matchId=" + matchId);
            sendError(session, "missing_fields");
            session.close(CloseStatus.POLICY_VIOLATION);
            return;
        }

        try {
            String userId = JwtUtil.validateToken(token).getSubject();
            System.out.println("[AUTH] Token validated: userId=" + userId);

            if (!matchService.isPlayerInMatch(userId, matchId)) {
                System.out.println("[AUTH] User not in match: userId=" + userId + " matchId=" + matchId);
                sendError(session, "not_in_match");
                session.close(CloseStatus.POLICY_VIOLATION);
                return;
            }

            session.getAttributes().put("userId", userId);
            session.getAttributes().put("matchId", matchId);
            pendingSessions.remove(session.getId());
            authenticatedSessions.put(userId, session);

            System.out.println("[AUTH] Auth successful: userId=" + userId + " matchId=" + matchId);
            System.out.println("[AUTH] Authenticated sessions: " + authenticatedSessions.size());

            MatchDto matchDto = matchService.getMatch(userId);
            GetProblemDto problemDto = problemRepository.getProblem(matchDto.getProblem_id()).get(0);
            System.out.print(problemDto.toString());
            session.sendMessage(new TextMessage("{\"type\":\"auth_ok\"}"));
            Map<String, Object> map = problemDto.toMap();
            map.put("type", "problem_info");
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(map)));
            List<TestCaseDto> testcases = problemRepository.getTestCases(matchDto.getProblem_id(), 3);
            Map<String, Object> testcasemap = new HashMap<>();
            testcasemap.put("type", "testcases_info");
            testcasemap.put("test_cases", testcases);
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(testcasemap)));

        } catch (Exception e) {
            System.out.println("[AUTH] Invalid token: " + e.getMessage());
            sendError(session, "invalid_token");
            session.close(CloseStatus.POLICY_VIOLATION);
        }
    }

    private void handleGameMessage(WebSocketSession session, Map<String, Object> payload) throws Exception {
        String userId = (String) session.getAttributes().get("userId");
        String type = (String) payload.get("type");
        System.out.println("[GAME] userId=" + userId + " type=" + type);

        switch (type) {
            case "ping" -> {
                System.out.println("[GAME] Ping received, sending pong: userId=" + userId);
                session.sendMessage(new TextMessage("{\"type\":\"pong\"}"));
            }
            case "submission" -> {
                System.out.println("[GAME] Submission received,userId=" + userId);
                submissionCheck(session, payload);
            }
            default -> {
                System.out.println("[GAME] Unknown message type: " + type);
                sendError(session, "unknown_type");
            }
        }
    }

    private void sendToOpponent(String userId, Map<String, Object> payload) {
        UUID opponentId = matchService.findOpponentId(userId);
        if (opponentId == null) {
            System.out.println("[OPPONENT] Opponent not found: userId=" + userId);
            return;
        }

        WebSocketSession opponentSession = authenticatedSessions.get(opponentId.toString());
        if (opponentSession != null && opponentSession.isOpen()) {
            try {
                System.out.println("[OPPONENT] Sending message: " + userId + " → " + opponentId);
                opponentSession.sendMessage(new TextMessage(objectMapper.writeValueAsString(payload)));
            } catch (IOException ignored) {
            }
        } else {
            System.out.println("[OPPONENT] Opponent not connected: opponentId=" + opponentId);
        }
    }

    @Scheduled(fixedDelay = 2000)
    public void evictUnauthenticatedSessions() {
        Instant threshold = Instant.now().minusSeconds(5);

        pendingSessions.values().forEach(session -> {
            Instant connectedAt = (Instant) session.getAttributes().get("connectedAt");
            if (connectedAt != null && connectedAt.isBefore(threshold)) {
                System.out.println("[TIMEOUT] No auth received, closing: sessionId=" + session.getId());
                try {
                    sendError(session, "auth_timeout");
                    session.close(CloseStatus.POLICY_VIOLATION);
                } catch (IOException ignored) {
                }
                pendingSessions.remove(session.getId());
            }
        });
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        pendingSessions.remove(session.getId());
        String userId = (String) session.getAttributes().get("userId");
        if (userId != null) {
            authenticatedSessions.remove(userId);
            sendToOpponent(userId, Map.of("type", "opponent_Disconnected"));
            System.out.println("[CLOSED] userId=" + userId + " disconnected, reason: " + status);
        } else {
            System.out.println("[CLOSED] Unauthenticated session closed: sessionId=" + session.getId());
        }
        System.out.println("[CLOSED] Authenticated sessions: " + authenticatedSessions.size());
    }

    private void sendError(WebSocketSession session, String message) throws IOException {
        if (session.isOpen()) {
            System.out.println("[ERROR] Sending error: sessionId=" + session.getId() + " message=" + message);
            session.sendMessage(new TextMessage(
                    "{\"type\":\"error\",\"message\":\"" + message + "\"}"));
        }
    }

    private void submissionCheck(WebSocketSession session, Map<String, Object> payload) throws IOException {
        String source_code = (String) payload.get("source_code");
        String language = (String) payload.get("language");
        System.out.println("source_code:" + source_code + "\nlanguage:" + language);
        UUID userId = UUID.fromString((String) session.getAttributes().get("userId"));
        UUID matchId = UUID.fromString((String) session.getAttributes().get("matchId"));
        MatchDto match = matchService.getMatch(userId.toString());
        List<TestCaseDto> testcases = problemRepository.getTestCases(match.getProblem_id());
        int tests_passed = 0;
        String verdict = "";

        int LanguageId = LanguageMap.get(language);

        RestClient judgeApi = RestClient.create();
        Map<String, Object> body = new HashMap<>();
        body.put("source_code", source_code);
        body.put("language_id", LanguageId);

        int testcasenum = 0;
        for (TestCaseDto testcase : testcases) {
            testcasenum++;
            body.put("stdin", testcase.getStdin());
            body.put("expected_output", testcase.getExpected_stdout());

            JsonNode responseBody = judgeApi.post()
                    .uri("http://localhost:2358/submissions/?base64_encoded=false&wait=true")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(objectMapper.writeValueAsString(body))
                    .retrieve()
                    .body(JsonNode.class);

            if (responseBody != null && responseBody.has("status")) {
                String StatusDescription = responseBody.path("status").path("description").asString();
                if (StatusDescription.equals("Accepted")) {
                    verdict = "ACCEPTED";
                    tests_passed++;
                } else {
                    System.out.println("test " + testcasenum + " failed with status: " + StatusDescription);

                    if (validStatuses.contains(StatusDescription.toUpperCase().replace(' ', '_'))) {
                        verdict = StatusDescription.toUpperCase().replace(' ', '_');
                    } else {
                        verdict = "RUNTIME_ERROR";
                    }

                    Map<String, Object> failureMsg = new HashMap<>();
                    failureMsg.put("type", "SUBMISSION_FAILED");
                    failureMsg.put("test_case_number", testcasenum);
                    failureMsg.put("status", StatusDescription);
                    failureMsg.put("message", "Test " + testcasenum + " failed with status: " + StatusDescription);

                    session.sendMessage(new TextMessage(objectMapper.writeValueAsString(failureMsg)));

                    break;
                }
            } else {
                break;
            }

        }
        if (tests_passed == testcases.size()) {
            UUID opponent = matchService.findOpponentId(userId.toString());
            System.out.println(opponent.toString());
            int rating_change = ratingService.calculateRatingChange(userId, opponent, matchId, 1);
            Map<String, String> msg = new HashMap<>();
            msg.put("type", "WIN");
            msg.put("rating_change", String.valueOf(rating_change));
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(msg)));
            Map<String, String> msg2 = new HashMap<>();
            msg2.put("type", "LOSE");
            msg2.put("rating_change", String.valueOf("-" + rating_change));
            System.out.println(authenticatedSessions.toString());
            authenticatedSessions.get(String.valueOf(opponent))
                    .sendMessage(new TextMessage(objectMapper.writeValueAsString(msg2)));
            matchService.removeMatch(userId);
            authenticatedSessions.get(String.valueOf(opponent)).close();
            session.close();

        }

        matchRepository.createSubmission(userId, matchId, source_code, language, verdict, tests_passed);
    }

}
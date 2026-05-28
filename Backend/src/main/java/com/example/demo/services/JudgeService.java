package com.example.demo.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.example.demo.dtos.TestCaseDto;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Service
public class JudgeService {

    // Maps Judge0 status descriptions to the verdict codes stored in the DB.
    private static final Map<String, String> VERDICT_MAP = Map.of(
            "Accepted",              "ACCEPTED",
            "Wrong Answer",          "WRONG_ANSWER",
            "Time Limit Exceeded",   "TIME_LIMIT",
            "Memory Limit Exceeded", "MEMORY_LIMIT",
            "Compilation Error",     "COMPILATION_ERROR");

    private static final Map<String, Integer> LANGUAGE_IDS = Map.of(
            "JAVA",   62,
            "PYTHON", 71);

    private final RestClient restClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public JudgeService() {
        this.restClient = RestClient.create();
    }

    JudgeService(RestClient restClient) {
        this.restClient = restClient;
    }

    // Evaluates all test cases for a submission and returns an aggregated result.
    public SubmissionResult evaluate(String sourceCode, String language, List<TestCaseDto> testCases) {
        int languageId = LANGUAGE_IDS.getOrDefault(language, 62);
        int testsPassed = 0;

        for (int i = 0; i < testCases.size(); i++) {
            TestCaseDto tc = testCases.get(i);
            String description = callJudge0(sourceCode, languageId, tc.getStdin(), tc.getExpected_stdout());

            if (description == null) {
                return SubmissionResult.failure("RUNTIME_ERROR", testsPassed, testCases.size(),
                        "Runtime Error", i + 1, null, null);
            }

            if ("Accepted".equals(description)) {
                testsPassed++;
            } else {
                String verdict = VERDICT_MAP.getOrDefault(description, "RUNTIME_ERROR");
                return SubmissionResult.failure(verdict, testsPassed, testCases.size(),
                        description, i + 1, null, tc.getExpected_stdout());
            }
        }

        return SubmissionResult.win(testCases.size());
    }

    
    String callJudge0(String sourceCode, int languageId, String stdin, String expectedOutput) {
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("source_code", sourceCode);
            body.put("language_id", languageId);
            body.put("stdin", stdin);
            body.put("expected_output", expectedOutput);

            JsonNode response = restClient.post()
                    .uri("http://localhost:2358/submissions/?base64_encoded=false&wait=true")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(objectMapper.writeValueAsString(body))
                    .retrieve()
                    .body(JsonNode.class);

            if (response != null && response.has("status")) {
                return response.path("status").path("description").asString();
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }
}

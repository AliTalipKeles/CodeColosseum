package com.example.demo;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;

import com.example.demo.controllers.Rest.ProblemController;
import com.example.demo.services.ProblemService;
import com.example.demo.util.JwtUtil;

/**
 * Integration slice test for the ProblemController HTTP layer.
 * Uses MockMvc standaloneSetup — no Spring context or DB required.
 * ProblemService is replaced with a Mockito mock.
 *
 * Covers: FR-1 (auth boundary), FR-6 (admin-only approval).
 */
@ExtendWith(MockitoExtension.class)
class ProblemControllerTest {

    MockMvc mockMvc;

    @Mock
    ProblemService problemService;

    @BeforeEach
    void setUp() {
        mockMvc = standaloneSetup(new ProblemController(problemService)).build();
    }

    // ─── TC-PC-01 ────────────────────────────────────────────────────────────────
    // Request without Authorization header must be rejected with 401 (FR-1).
    @Test
    @DisplayName("TC-PC-01: POST /problem/createrequest without token returns 401")
    void createRequest_withoutToken_returns401() throws Exception {
        mockMvc.perform(post("/problem/createrequest")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isUnauthorized());
    }

    // ─── TC-PC-02 ────────────────────────────────────────────────────────────────
    // USER-role token must be rejected with 403 on admin-only endpoint (FR-6).
    @Test
    @DisplayName("TC-PC-02: PUT /problem/setapproved with USER role returns 403")
    void setApproved_withUserRole_returns403() throws Exception {
        String token = JwtUtil.generateToken(UUID.randomUUID(), "testuser", "USER");

        mockMvc.perform(put("/problem/setapproved/SomeProblem")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isForbidden());
    }

    // ─── TC-PC-03 ────────────────────────────────────────────────────────────────
    // Valid ADMIN token with no query params → delegates to service and returns 200.
    @Test
    @DisplayName("TC-PC-03: GET /problem with valid ADMIN token returns 200")
    void getProblems_withValidAdminToken_returns200() throws Exception {
        String token = JwtUtil.generateToken(UUID.randomUUID(), "admin", "ADMIN");
        doReturn(ResponseEntity.ok(Map.of("data", List.of())))
            .when(problemService).getProblems(null, null);

        mockMvc.perform(get("/problem")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk());
    }
}

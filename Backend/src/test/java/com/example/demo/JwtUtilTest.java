package com.example.demo;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.example.demo.util.JwtUtil;

import io.jsonwebtoken.Claims;

class JwtUtilTest {

    // ─── TC-JWT-01 ───────────────────────────────────────────────────────────────
    @Test
    @DisplayName("TC-JWT-01: Generated token carries correct username claim")
    void token_containsUsername() {
        UUID id = UUID.randomUUID();

        String token  = JwtUtil.generateToken(id, "alice", "USER");
        Claims claims = JwtUtil.validateToken(token);

        assertNotNull(claims);
        assertEquals("alice", claims.get("username"));
    }

    // ─── TC-JWT-02 ───────────────────────────────────────────────────────────────
    @Test
    @DisplayName("TC-JWT-02: Generated token carries correct role claim")
    void token_containsRole() {
        UUID id = UUID.randomUUID();

        String token  = JwtUtil.generateToken(id, "alice", "ADMIN");
        Claims claims = JwtUtil.validateToken(token);

        assertNotNull(claims);
        assertEquals("ADMIN", claims.get("role"));
    }

    // ─── TC-JWT-03 ───────────────────────────────────────────────────────────────
    @Test
    @DisplayName("TC-JWT-03: Token subject equals the UUID that was passed in")
    void token_subjectIsUserId() {
        UUID id = UUID.randomUUID();

        String token  = JwtUtil.generateToken(id, "alice", "USER");
        Claims claims = JwtUtil.validateToken(token);

        assertNotNull(claims);
        assertEquals(id.toString(), claims.getSubject());
    }

    // ─── TC-JWT-04 ───────────────────────────────────────────────────────────────
    // A token whose signature has been altered must be rejected.
    @Test
    @DisplayName("TC-JWT-04: Tampered token (altered signature) returns null")
    void tamperedToken_returnsNull() {
        UUID id = UUID.randomUUID();
        String token   = JwtUtil.generateToken(id, "alice", "USER");
        String tampered = token.substring(0, token.length() - 4) + "XXXX";

        assertNull(JwtUtil.validateToken(tampered));
    }

    // ─── TC-JWT-05 ───────────────────────────────────────────────────────────────
    @Test
    @DisplayName("TC-JWT-05: Completely invalid string returns null")
    void invalidToken_returnsNull() {
        assertNull(JwtUtil.validateToken("this.is.not.a.jwt"));
    }
}

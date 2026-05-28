package com.example.demo;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Method;
import java.time.Instant;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.socket.WebSocketSession;

import com.example.demo.dtos.MatchmakingDto;
import com.example.demo.repositories.MatchRepository;
import com.example.demo.services.WebSocket.MatchService;
import com.example.demo.services.WebSocket.MatchmakingService;

@ExtendWith(MockitoExtension.class)
class MatchmakingTest {

    @Mock MatchRepository matchRepository;
    @Mock MatchService    matchService;
    @Mock WebSocketSession session;

    @InjectMocks
    MatchmakingService matchmakingService;

    // Convenience: fresh queue entry (queueEntryDate = now)
    MatchmakingDto freshPlayer(String id, int elo) {
        return new MatchmakingDto(id, "player-" + id, elo, session);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // canMatchWith — Elo tolerance algorithm
    // Tolerance for a freshly queued player = 50 + (0 intervals × 50) = 50.
    // Tolerance grows by 50 every 30 seconds.
    // effectiveTolerance = min(tolerance_p1, tolerance_p2)
    // ═══════════════════════════════════════════════════════════════════════════

    // ─── TC-MM-01 ────────────────────────────────────────────────────────────────
    @Test
    @DisplayName("TC-MM-01: Identical Elo (diff=0) can always match immediately")
    void sameElo_canMatch() {
        MatchmakingDto p1 = freshPlayer("1", 1000);
        MatchmakingDto p2 = freshPlayer("2", 1000);
        assertTrue(p1.canMatchWith(p2));
    }

    // ─── TC-MM-02 ────────────────────────────────────────────────────────────────
    // Boundary: diff exactly equals initial tolerance (50) → must be allowed.
    @Test
    @DisplayName("TC-MM-02: Elo diff exactly at initial tolerance (50) is accepted (boundary)")
    void eloDiff_exactlyAtTolerance_isAllowed() {
        MatchmakingDto p1 = freshPlayer("1", 1050);
        MatchmakingDto p2 = freshPlayer("2", 1000);
        assertTrue(p1.canMatchWith(p2));
    }

    // ─── TC-MM-03 ────────────────────────────────────────────────────────────────
    // Boundary: diff one above tolerance (51) → must be rejected.
    @Test
    @DisplayName("TC-MM-03: Elo diff one above tolerance (51) is rejected (boundary)")
    void eloDiff_oneAboveTolerance_isRejected() {
        MatchmakingDto p1 = freshPlayer("1", 1051);
        MatchmakingDto p2 = freshPlayer("2", 1000);
        assertFalse(p1.canMatchWith(p2));
    }

    // ─── TC-MM-04 ────────────────────────────────────────────────────────────────
    // Both players have waited 35 s → 1 full 30-s interval → tolerance = 100.
    // Elo diff = 100 must be accepted.
    @Test
    @DisplayName("TC-MM-04: Both waited 35 s — tolerance grows to 100, diff=100 accepted")
    void bothWaited35s_toleranceGrows() {
        MatchmakingDto p1 = freshPlayer("1", 1100);
        MatchmakingDto p2 = freshPlayer("2", 1000);
        Instant ago = Instant.now().minusSeconds(35);
        p1.setQueueEntryDate(ago);
        p2.setQueueEntryDate(ago);

        assertTrue(p1.canMatchWith(p2));
    }

    // ─── TC-MM-05 ────────────────────────────────────────────────────────────────
    // effectiveTolerance = min(p1_tol, p2_tol).
    // p1 waited 35 s (tol=100), p2 is fresh (tol=50).
    // effective = min(100, 50) = 50.  diff=70 > 50 → rejected.
    @Test
    @DisplayName("TC-MM-05: Long-waiting player cannot override fresh opponent's low tolerance")
    void effectiveToleranceIsMinimum() {
        MatchmakingDto p1 = freshPlayer("1", 1070);
        MatchmakingDto p2 = freshPlayer("2", 1000);
        p1.setQueueEntryDate(Instant.now().minusSeconds(35));

        assertFalse(p1.canMatchWith(p2));
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // calculateDifficulty (private) — boundary value analysis
    // EASY   : avg <= 1200
    // MEDIUM : 1200 < avg <= 1500
    // HARD   : avg > 1500
    // ═══════════════════════════════════════════════════════════════════════════

    private String invokeDifficulty(int elo1, int elo2) throws Exception {
        Method m = MatchmakingService.class
                .getDeclaredMethod("calculateDifficulty", int.class, int.class);
        m.setAccessible(true);
        return (String) m.invoke(matchmakingService, elo1, elo2);
    }

    // ─── TC-DIFF-01 ──────────────────────────────────────────────────────────────
    @Test
    @DisplayName("TC-DIFF-01: Average Elo ≤ 1200 → EASY (includes boundary avg=1200)")
    void avgElo_atOrBelow1200_returnsEasy() throws Exception {
        assertEquals("EASY", invokeDifficulty(1000, 1000)); // avg = 1000
        assertEquals("EASY", invokeDifficulty(1200, 1200)); // avg = 1200 (boundary)
    }

    // ─── TC-DIFF-02 ──────────────────────────────────────────────────────────────
    @Test
    @DisplayName("TC-DIFF-02: 1200 < Average Elo ≤ 1500 → MEDIUM (includes boundary avg=1500)")
    void avgElo_between1200and1500_returnsMedium() throws Exception {
        assertEquals("MEDIUM", invokeDifficulty(1202, 1200)); // avg = 1201
        assertEquals("MEDIUM", invokeDifficulty(1500, 1500)); // avg = 1500 (boundary)
    }

    // ─── TC-DIFF-03 ──────────────────────────────────────────────────────────────
    @Test
    @DisplayName("TC-DIFF-03: Average Elo > 1500 → HARD")
    void avgElo_above1500_returnsHard() throws Exception {
        assertEquals("HARD", invokeDifficulty(1502, 1500)); // avg = 1501 (just above boundary)
        assertEquals("HARD", invokeDifficulty(2000, 2000)); // avg = 2000
    }
}

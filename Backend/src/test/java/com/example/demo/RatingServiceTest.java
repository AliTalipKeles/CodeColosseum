package com.example.demo;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.example.demo.repositories.RatingRepository;
import com.example.demo.repositories.UserRepository;
import com.example.demo.services.RatingService;

@ExtendWith(MockitoExtension.class)
class RatingServiceTest {

    @Mock
    UserRepository userRepository;

    @Mock
    RatingRepository ratingRepository;

    @InjectMocks
    RatingService ratingService;

    UUID user1;
    UUID user2;
    UUID matchId;

    @BeforeEach
    void setUp() {
        user1  = UUID.randomUUID();
        user2  = UUID.randomUUID();
        matchId = UUID.randomUUID();
    }

    // ─── TC-RS-01 ────────────────────────────────────────────────────────────────
    // Equal Elo (1000 vs 1000): expected score = 0.5
    // Win delta = round(32 * (1.0 - 0.5)) = 16
    @Test
    @DisplayName("TC-RS-01: Winner gains +16 when both players have equal Elo (1000)")
    void equalElo_winnerGainsCorrectRating() {
        when(userRepository.getUserElo(user1)).thenReturn(1000);
        when(userRepository.getUserElo(user2)).thenReturn(1000);

        int delta = ratingService.calculateRatingChange(user1, user2, matchId, 1);

        assertEquals(16, delta);
    }

    // ─── TC-RS-02 ────────────────────────────────────────────────────────────────
    // Equal Elo (1000 vs 1000): Loss delta = round(32 * (0 - 0.5)) = -16
    @Test
    @DisplayName("TC-RS-02: Loser loses -16 when both players have equal Elo (1000)")
    void equalElo_loserLosesCorrectRating() {
        when(userRepository.getUserElo(user1)).thenReturn(1000);
        when(userRepository.getUserElo(user2)).thenReturn(1000);

        int delta = ratingService.calculateRatingChange(user1, user2, matchId, -1);

        assertEquals(-16, delta);
    }

    // ─── TC-RS-03 ────────────────────────────────────────────────────────────────
    // Equal Elo draw: delta = round(32 * (0.5 - 0.5)) = 0
    @Test
    @DisplayName("TC-RS-03: Draw results in zero rating change for equal-Elo players")
    void equalElo_drawGivesZeroChange() {
        when(userRepository.getUserElo(user1)).thenReturn(1000);
        when(userRepository.getUserElo(user2)).thenReturn(1000);

        int delta = ratingService.calculateRatingChange(user1, user2, matchId, 0);

        assertEquals(0, delta);
    }

    // ─── TC-RS-04 ────────────────────────────────────────────────────────────────
    // Rating floor: elo1=10, elo2=10, loss
    // newElo1 = max(0, round(10 + 32*(0 - 0.5))) = max(0, round(-6)) = 0
    // delta returned = 0 - 10 = -10
    // The DB must be updated with 0, not a negative value.
    @Test
    @DisplayName("TC-RS-04: Rating floor clamps Elo at 0 — DB update receives 0 not negative")
    void ratingFloor_eloNeverGoesBelowZero() {
        when(userRepository.getUserElo(user1)).thenReturn(10);
        when(userRepository.getUserElo(user2)).thenReturn(10);

        int delta = ratingService.calculateRatingChange(user1, user2, matchId, -1);

        assertEquals(-10, delta);
        verify(ratingRepository).updateUserElo(eq(user1), eq(0));
    }

    // ─── TC-RS-05 ────────────────────────────────────────────────────────────────
    // Favourite wins: elo1=1200, elo2=800
    // expected1 = 1/(1+10^((800-1200)/400)) = 1/(1+10^(-1)) ≈ 0.9091
    // delta = round(32 * (1.0 - 0.9091)) = round(2.909) = 3
    @Test
    @DisplayName("TC-RS-05: High-Elo (1200) beating low-Elo (800) gains only +3 (small upset margin)")
    void favouriteWins_smallRatingGain() {
        when(userRepository.getUserElo(user1)).thenReturn(1200);
        when(userRepository.getUserElo(user2)).thenReturn(800);

        int delta = ratingService.calculateRatingChange(user1, user2, matchId, 1);

        assertEquals(3, delta);
    }
}

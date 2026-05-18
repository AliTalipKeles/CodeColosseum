package com.example.demo.services.WebSocket;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.example.demo.dtos.MatchDto;@Service
public class MatchService {

    private final ConcurrentHashMap<UUID, MatchDto> userMatchMap = new ConcurrentHashMap<>();

    public void addMatch(MatchDto matchDto) {
        userMatchMap.put(matchDto.getContestant_a_id(), matchDto);
        userMatchMap.put(matchDto.getContestant_b_id(), matchDto);
        System.out.println("[MATCH] Match added: matchId=" + matchDto.getMatch_id());
        System.out.println("[MATCH] Players: " + matchDto.getContestant_a_id() + " vs " + matchDto.getContestant_b_id());
        System.out.println("[MATCH] Total active players: " + userMatchMap.size());
    }

    public boolean isPlayerInMatch(String userId, String matchId) {
        try {
            MatchDto match = userMatchMap.get(UUID.fromString(userId));
            boolean result = match != null && match.getMatch_id().equals(UUID.fromString(matchId));
            System.out.println("[MATCH] isPlayerInMatch: userId=" + userId + " matchId=" + matchId + " → " + result);
            return result;
        } catch (IllegalArgumentException e) {
            System.out.println("[MATCH] Invalid UUID: " + e.getMessage());
            return false;
        }
    }

    public UUID findOpponentId(String userId) {
        MatchDto match = userMatchMap.get(UUID.fromString(userId));
        if (match == null) {
            System.out.println("[MATCH] Match not found: userId=" + userId);
            return null;
        }
        UUID opponentId = match.getOpponentId(UUID.fromString(userId));
        System.out.println("[MATCH] Opponent found: userId=" + userId + " → opponentId=" + opponentId);
        return opponentId;
    }

    public MatchDto getMatch(String userId) {
        MatchDto match = userMatchMap.get(UUID.fromString(userId));
        System.out.println("[MATCH] getMatch: userId=" + userId + " → " + (match != null ? match.getMatch_id() : "not found"));
        return match;
    }

    public void removeMatch(UUID userId) {
        MatchDto match = userMatchMap.get(userId);
        if (match == null) {
            System.out.println("[MATCH] Match not found to remove: userId=" + userId);
            return;
        }
        userMatchMap.remove(match.getContestant_a_id());
        userMatchMap.remove(match.getContestant_b_id());
        System.out.println("[MATCH] Match removed: matchId=" + match.getMatch_id());
        System.out.println("[MATCH] Total active players: " + userMatchMap.size());
    }
}

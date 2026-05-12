package com.example.demo.dtos;

import java.time.Duration;
import java.time.Instant;

import org.springframework.web.socket.WebSocketSession;

import lombok.Data;

@Data
public class MatchmakingDto {

    private String id;
    private String username;
    private int elo;
    private Instant queueEntryDate;
    private WebSocketSession session;

    public MatchmakingDto(String id,String username, int elo, WebSocketSession session) {
        this.id = id;
        this.username = username;
        this.elo = elo;
        this.queueEntryDate = Instant.now();
        this.session = session;
    }

    public String getSessionId() {
        return session.getId();
    }

    public long getWaitingSeconds() {
        return Duration.between(queueEntryDate, Instant.now()).getSeconds();
    }

    public int getEloTolerance() {
        long intervals = getWaitingSeconds() / 30;
        return (int) (50 + (intervals * 50));
    }

    public boolean canMatchWith(MatchmakingDto other) {
        int eloDiff = Math.abs(this.elo - other.elo);
        int effectiveTolerance = Math.min(this.getEloTolerance(), other.getEloTolerance());
        return eloDiff <= effectiveTolerance;
    }

}
package com.example.demo.controllers.WebSocket;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.example.demo.dtos.MatchmakingDto;
import com.example.demo.services.UserService;
import com.example.demo.services.WebSocket.MatchmakingService;
import com.example.demo.util.JwtUtil;

import io.jsonwebtoken.Claims;
import tools.jackson.databind.ObjectMapper;


@Component
public class MatchmakingHandler extends TextWebSocketHandler {

    private final MatchmakingService matchmakingService;
    private final UserService userService;
    private final Map<String, WebSocketSession> connectedSessions = new ConcurrentHashMap<>();
    private final ObjectMapper mapper = new ObjectMapper();

    public MatchmakingHandler(MatchmakingService matchmakingService,UserService userService) {
        this.matchmakingService = matchmakingService;
        this.userService = userService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String query = session.getUri().getQuery();
        
        if (query == null || !query.contains("token=")) {
            session.close(CloseStatus.NOT_ACCEPTABLE);
            return;
        }

        String token = query.replace("token=", "");
        Claims claim = JwtUtil.validateToken(token);
        if(claim == null){
            session.close(CloseStatus.NOT_ACCEPTABLE);
            return;
        }

        // token'dan username çek
        String username = (String)claim.get("username");

        if (matchmakingService.isInQueue(username)) {
            session.close(CloseStatus.NOT_ACCEPTABLE);
            return;
        }

        int elo = userService.getUserElo(username);

        MatchmakingDto player = new MatchmakingDto(username, elo, session);
        matchmakingService.addPlayer(player);

        session.sendMessage(new TextMessage("{\"event\":\"CONNECTED\"}"));
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Map<String, Object> json = mapper.readValue(message.getPayload(), Map.class);
        String action = (String) json.get("action");

        if ("LEAVE".equals(action)) {
            Map<String,Object> response = new HashMap<>();
            response.put("event", "LEFT");
            session.sendMessage(new TextMessage(mapper.writeValueAsString(response)));
            session.close();
        }
    }
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        matchmakingService.removePlayer(session.getId());
        connectedSessions.remove(session.getId());
    }
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        matchmakingService.removePlayer(session.getId());
        connectedSessions.remove(session.getId());
    }
}
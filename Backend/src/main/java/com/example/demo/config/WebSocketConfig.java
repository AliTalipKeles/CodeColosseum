package com.example.demo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import com.example.demo.controllers.WebSocket.MatchmakingHandler;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final MatchmakingHandler matchmakingHandler;

    public WebSocketConfig(MatchmakingHandler matchmakingHandler) {
        this.matchmakingHandler = matchmakingHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(matchmakingHandler, "/matchmaking")
                .setAllowedOrigins("*");
    }
}
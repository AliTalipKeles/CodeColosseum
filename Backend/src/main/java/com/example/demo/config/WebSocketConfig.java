package com.example.demo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import com.example.demo.controllers.WebSocket.MatchHandler;
import com.example.demo.controllers.WebSocket.MatchmakingHandler;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final MatchmakingHandler matchmakingHandler;
    private final MatchHandler matchHandler;

    public WebSocketConfig(MatchmakingHandler matchmakingHandler,MatchHandler matchHandler) {
        this.matchmakingHandler = matchmakingHandler;
        this.matchHandler = matchHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(matchmakingHandler, "/matchmaking")
                .setAllowedOrigins("*");

        registry.addHandler(matchHandler,"/match")
                .setAllowedOrigins("*");
    }
}
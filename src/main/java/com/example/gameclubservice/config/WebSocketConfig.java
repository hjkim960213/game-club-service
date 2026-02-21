package com.example.gameclubservice.config;

import com.example.gameclubservice.handler.ChatHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.*;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {
    private final ChatHandler chatHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(chatHandler, "/ws/chat")
                .setAllowedOrigins("*")
                // 🚩 이 인터셉터가 로그인의 'role', 'nickname' 정보를 웹소켓으로 가져옵니다.
                .addInterceptors(new HttpSessionHandshakeInterceptor());
    }
}
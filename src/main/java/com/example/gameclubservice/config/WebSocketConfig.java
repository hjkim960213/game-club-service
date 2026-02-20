package com.example.gameclubservice.config;

import com.example.gameclubservice.handler.ChatHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket // 웹소켓 활성화
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final ChatHandler chatHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // "/ws/chat" 주소로 연결하면 채팅 핸들러가 작동하도록 설정
        // setAllowedOrigins("*")는 어디서든 접속 가능하게 해줍니다.
        registry.addHandler(chatHandler, "/ws/chat").setAllowedOrigins("*");
    }
}
package com.example.gameclubservice.handler;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import java.util.concurrent.*;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

@Component
public class ChatHandler extends TextWebSocketHandler {
    private static final Map<WebSocketSession, String> sessionNames = new ConcurrentHashMap<>();
    private static final List<String> chatHistory = new CopyOnWriteArrayList<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // 인터셉터가 가져온 세션 속성 꺼내기
        String nickname = (String) session.getAttributes().get("nickname");
        if (nickname == null) nickname = "익명";

        sessionNames.put(session, nickname);

        // 과거 대화 기록 전송
        for (String msg : chatHistory) {
            if (session.isOpen()) session.sendMessage(new TextMessage(msg));
        }
        broadcastUserList();
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload().trim();
        String nickname = (String) session.getAttributes().get("nickname");
        String role = (String) session.getAttributes().get("role");

        // 🛠️ 관리자 명령어 처리 (/clear, /공지)
        if (payload.startsWith("/clear") || payload.startsWith("/공지")) {
            // ADMIN 역할이거나 닉네임이 '운영진'인 경우만 허용
            if ("ADMIN".equals(role) || "운영진".equals(nickname)) {
                if (payload.startsWith("/clear")) {
                    executeClear(payload);
                } else if (payload.startsWith("/공지 ")) {
                    String notice = payload.replace("/공지 ", "");
                    broadcast("[NOTICE]" + notice);
                }
                return; // 명령어는 채팅 기록에 남기지 않음
            } else {
                session.sendMessage(new TextMessage("🚫 권한이 없습니다."));
                return;
            }
        }

        // 일반 채팅 저장 및 전송
        chatHistory.add(payload);
        if (chatHistory.size() > 100) chatHistory.remove(0);
        broadcast(payload);
    }

    private void executeClear(String payload) throws Exception {
        String[] parts = payload.split(" ");
        if (parts.length == 1 || "all".equals(parts[1])) {
            chatHistory.clear();
            broadcast("[CLEAR_CHAT]");
        } else {
            try {
                int count = Integer.parseInt(parts[1]);
                int removeSize = Math.min(count, chatHistory.size());
                for (int i = 0; i < removeSize; i++) {
                    chatHistory.remove(chatHistory.size() - 1);
                }
                broadcast("[CLEAR_COUNT]" + count);
            } catch (Exception e) { /* 무시 */ }
        }
    }

    private void broadcast(String msg) throws Exception {
        for (WebSocketSession s : sessionNames.keySet()) {
            if (s.isOpen()) s.sendMessage(new TextMessage(msg));
        }
    }

    private void broadcastUserList() throws Exception {
        StringJoiner sj = new StringJoiner(", ");
        sessionNames.values().forEach(sj::add);
        broadcast("[USER_LIST]" + sessionNames.size() + "명 접속 중: " + sj.toString());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessionNames.remove(session);
        broadcastUserList();
    }
}
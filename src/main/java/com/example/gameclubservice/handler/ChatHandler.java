package com.example.gameclubservice.handler;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

@Component
public class ChatHandler extends TextWebSocketHandler {

    private static final Map<WebSocketSession, String> sessionNames = new ConcurrentHashMap<>();
    private static final List<String> chatHistory = new CopyOnWriteArrayList<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String query = session.getUri().getQuery();
        String nickname = "익명";

        if (query != null && query.contains("nickname=")) {
            String rawNickname = query.split("nickname=")[1].split("&")[0];
            nickname = URLDecoder.decode(rawNickname, StandardCharsets.UTF_8);
        }

        // 🚩 [범인 검거 및 해결] 세션에 정보를 저장해야 나중에 꺼내 쓸 수 있습니다!
        session.getAttributes().put("nickname", nickname);

        sessionNames.put(session, nickname);

        // 과거 채팅 기록 전송
        for (String msg : chatHistory) {
            if (session.isOpen()) {
                session.sendMessage(new TextMessage(msg));
            }
        }

        broadcastUserList();
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();

        // 🚩 이제 여기서 nickname을 꺼내면 "운영진"이 제대로 나옵니다.
        String nickname = (String) session.getAttributes().get("nickname");

        // 1️⃣ 명령어 가로채기 (/clear, /공지)
        if (payload.startsWith("/clear") || payload.startsWith("/공지")) {

            // 권한 체크: 닉네임이 정확히 "운영진"일 때만 허용
            if ("운영진".equals(nickname)) {

                // --- 전체/부분 삭제 로직 ---
                if (payload.startsWith("/clear")) {
                    String[] parts = payload.split(" ");
                    if (parts.length == 1 || "all".equals(parts[1])) {
                        chatHistory.clear();
                        broadcast("[CLEAR_CHAT]");
                    } else {
                        try {
                            int count = Integer.parseInt(parts[1]);
                            for (int i = 0; i < Math.min(count, chatHistory.size()); i++) {
                                chatHistory.remove(chatHistory.size() - 1);
                            }
                            broadcast("[CLEAR_COUNT]" + count);
                        } catch (Exception e) {
                            session.sendMessage(new TextMessage("⚠️ 숫자를 입력해주세요."));
                        }
                    }
                }
                // --- 공지사항 로직 ---
                else if (payload.startsWith("/공지 ")) {
                    String notice = payload.replace("/공지 ", "");
                    broadcast("[NOTICE]" + notice);
                }

                return; // 🚩 명령어를 처리했으므로 일반 채팅 저장을 건너뜁니다.
            } else {
                session.sendMessage(new TextMessage("🚫 권한이 없습니다. (현재 닉네임: " + nickname + ")"));
                return;
            }
        }

        // 2️⃣ 일반 채팅 처리
        chatHistory.add(payload);
        if (chatHistory.size() > 100) chatHistory.remove(0);
        broadcast(payload);
    }

    private void broadcast(String msg) throws Exception {
        for (WebSocketSession s : sessionNames.keySet()) {
            if (s.isOpen()) s.sendMessage(new TextMessage(msg));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessionNames.remove(session);
        broadcastUserList();
    }

    private void broadcastUserList() throws Exception {
        StringJoiner joiner = new StringJoiner(", ");
        for (String name : sessionNames.values()) joiner.add(name);
        broadcast("[USER_LIST]" + sessionNames.size() + "명 접속 중: " + joiner.toString());
    }
}
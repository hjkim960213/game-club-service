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
        String role = "GUEST"; // 기본 권한

        if (query != null) {
            String[] params = query.split("&");
            for (String param : params) {
                if (param.startsWith("nickname=")) {
                    nickname = URLDecoder.decode(param.split("=")[1], StandardCharsets.UTF_8);
                }
                // 🚩 주소창에 role=ADMIN이 있거나 닉네임이 운영진이면 권한 부여
                if (param.startsWith("role=")) {
                    role = param.split("=")[1];
                }
            }
        }

        if (nickname.equals("운영진")) {
            role = "ADMIN";
        }

        // 🚩 [중요] 세션 어트리뷰트에 직접 정보를 넣어줘야 handleTextMessage에서 꺼내 쓸 수 있습니다.
        session.getAttributes().put("nickname", nickname);
        session.getAttributes().put("role", role);

        sessionNames.put(session, nickname);

        // 과거 기록 전송
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

        // afterConnectionEstablished에서 넣어준 정보 꺼내기
        String nickname = (String) session.getAttributes().get("nickname");
        String role = (String) session.getAttributes().get("role");

        // 1️⃣ 관리자 명령어(/clear, /공지) 처리
        if (payload.startsWith("/clear") || payload.startsWith("/공지")) {
            if ("ADMIN".equals(role) || "운영진".equals(nickname)) {

                // --- 채팅 삭제 로직 ---
                if (payload.startsWith("/clear")) {
                    String[] parts = payload.split(" ");
                    if (parts.length == 1 || "all".equals(parts[1])) {
                        chatHistory.clear();
                        broadcast("[CLEAR_CHAT]");
                    } else {
                        try {
                            int count = Integer.parseInt(parts[1]);
                            int currentSize = chatHistory.size();
                            int removeLimit = Math.min(count, currentSize);
                            for (int i = 0; i < removeLimit; i++) {
                                chatHistory.remove(chatHistory.size() - 1);
                            }
                            broadcast("[CLEAR_COUNT]" + count);
                        } catch (NumberFormatException e) {
                            session.sendMessage(new TextMessage("⚠️ 숫자를 입력해주세요. (예: /clear 5)"));
                        }
                    }
                }
                // --- 공지사항 로직 ---
                else if (payload.startsWith("/공지 ")) {
                    String notice = payload.replace("/공지 ", "");
                    broadcast("[NOTICE]" + notice);
                }
            } else {
                session.sendMessage(new TextMessage("🚫 권한이 없습니다."));
            }
            return; // 명령어는 기록에 저장하지 않고 종료
        }

        // 2️⃣ 일반 채팅 처리
        chatHistory.add(payload);
        if (chatHistory.size() > 100) {
            chatHistory.remove(0);
        }
        broadcast(payload);
    }

    private void broadcast(String msg) throws Exception {
        for (WebSocketSession s : sessionNames.keySet()) {
            if (s.isOpen()) {
                s.sendMessage(new TextMessage(msg));
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessionNames.remove(session);
        broadcastUserList();
    }

    private void broadcastUserList() throws Exception {
        StringJoiner joiner = new StringJoiner(", ");
        for (String name : sessionNames.values()) {
            joiner.add(name);
        }
        String listMessage = "[USER_LIST]" + sessionNames.size() + "명 접속 중: " + joiner.toString();
        broadcast(listMessage);
    }
}
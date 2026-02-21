package com.example.gameclubservice.handler;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
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
        // 🚩 1. 불안정한 인터셉터 대신, 웹소켓 연결 주소(URL)에서 닉네임을 직접 뽑아옵니다.
        String query = session.getUri().getQuery();
        String nickname = "익명";
        String role = "GUEST";

        if (query != null && query.contains("nickname=")) {
            String rawNickname = query.split("nickname=")[1].split("&")[0];
            nickname = URLDecoder.decode(rawNickname, StandardCharsets.UTF_8);
        }

        // 🚩 2. 뽑아온 닉네임이 '운영진'이면 묻지도 따지지도 않고 ADMIN 권한을 줍니다.
        if ("운영진".equals(nickname)) {
            role = "ADMIN";
        }

        // 🚩 3. 메시지를 칠 때 서버가 헷갈리지 않게 세션 주머니에 단단히 묶어둡니다.
        session.getAttributes().put("nickname", nickname);
        session.getAttributes().put("role", role);

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

        // 🚩 4. 위에서 확실하게 저장한 닉네임과 권한을 꺼냅니다. (유실 방지)
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
                // 권한 오류 시 서버가 닉네임을 어떻게 인식했는지 확인하도록 메시지 수정
                session.sendMessage(new TextMessage("<span style='color:red;'>🚫 권한이 없습니다. (인식된 계정: " + nickname + ")</span>"));
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
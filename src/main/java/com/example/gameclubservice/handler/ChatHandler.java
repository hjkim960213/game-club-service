package com.example.gameclubservice.handler; // 본인 패키지명 확인 필수!

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

    // 1. [접속자 명단용] 현재 접속 중인 세션과 닉네임을 짝지어서 보관합니다. (스레드 안전)
    private static final Map<WebSocketSession, String> sessionNames = new ConcurrentHashMap<>();

    // 2. [채팅 기록용] 이전 채팅 기록을 최대 100개까지 보관합니다.
    private static final List<String> chatHistory = new CopyOnWriteArrayList<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // 프론트엔드에서 보낸 주소(URI)에서 닉네임만 쏙 빼냅니다.
        String query = session.getUri().getQuery();
        String nickname = "익명";
        if (query != null && query.contains("nickname=")) {
            String rawNickname = query.split("nickname=")[1].split("&")[0];
            nickname = URLDecoder.decode(rawNickname, StandardCharsets.UTF_8);
        }

        // 명단에 방금 들어온 사람을 등록합니다.
        sessionNames.put(session, nickname);

        // 🚩 [기능 1] 방금 들어온 사람에게만 과거 채팅 기록 쏴주기
        for (String msg : chatHistory) {
            if (session.isOpen()) {
                session.sendMessage(new TextMessage(msg));
            }
        }

        // 🚩 [기능 2] 누군가 들어왔으니 전체 유저에게 최신 명단 뿌리기
        broadcastUserList();
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();

        // 🚩 누군가 채팅을 치면 기록(History)에 먼저 저장합니다.
        chatHistory.add(payload);

        // 메모리가 터지지 않도록 최근 100개의 대화만 유지합니다.
        if (chatHistory.size() > 100) {
            chatHistory.remove(0);
        }

        // 접속 중인 모든 사람에게 채팅 메시지 전달
        for (WebSocketSession s : sessionNames.keySet()) {
            if (s.isOpen()) {
                s.sendMessage(new TextMessage(payload));
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        // 나간 사람을 명단에서 제거합니다.
        sessionNames.remove(session);
        // 누군가 나갔으니 남은 사람들에게 명단을 갱신해서 뿌려줍니다.
        broadcastUserList();
    }

    // ⭐ 현재 접속 중인 유저 명단을 조립해서 모두에게 쏘는 특별한 메서드
    private void broadcastUserList() throws Exception {
        StringJoiner joiner = new StringJoiner(", ");
        for (String name : sessionNames.values()) {
            joiner.add(name);
        }

        // 프론트엔드가 '이건 채팅이 아니라 명단이구나!' 하고 눈치챌 수 있게 [USER_LIST] 꼬리표를 달아줍니다.
        String listMessage = "[USER_LIST]" + sessionNames.size() + "명 접속 중: " + joiner.toString();

        for (WebSocketSession s : sessionNames.keySet()) {
            if (s.isOpen()) {
                s.sendMessage(new TextMessage(listMessage));
            }
        }
    }
}
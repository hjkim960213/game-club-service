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
        String nickname = (String) session.getAttributes().get("nickname");
        String role = (String) session.getAttributes().get("role");

        // 1️⃣ 관리자 명령어(/clear) 처리 루틴
        if (payload.startsWith("/clear")) {
            // 권한 확인: 닉네임이 '운영진'이거나 역할이 'ADMIN'인 경우만 허용
            if ("ADMIN".equals(role) || "운영진".equals(nickname)) {
                String[] parts = payload.split(" ");

                // Case A: /clear 또는 /clear all (전체 삭제)
                if (parts.length == 1 || "all".equals(parts[1])) {
                    chatHistory.clear(); // 서버 메모리 비우기
                    broadcast("[CLEAR_CHAT]"); // 모든 클라이언트에게 전체 삭제 신호
                }
                // Case B: /clear 5 (특정 개수 삭제)
                else {
                    try {
                        int count = Integer.parseInt(parts[1]);

                        // 서버 기록(History)에서 실제로 제거 (뒤에서부터 삭제)
                        int currentSize = chatHistory.size();
                        int removeLimit = Math.min(count, currentSize);
                        for (int i = 0; i < removeLimit; i++) {
                            chatHistory.remove(chatHistory.size() - 1);
                        }

                        // 클라이언트들에게 "뒤에서부터 X개 지워라"고 신호 보냄
                        broadcast("[CLEAR_COUNT]" + count);
                    } catch (NumberFormatException e) {
                        session.sendMessage(new TextMessage("⚠️ 숫자를 입력해주세요. (예: /clear 5)"));
                    }
                }
            } else {
                session.sendMessage(new TextMessage("🚫 삭제 권한이 없습니다."));
            }
            return; // 🚩 중요: 명령어는 아래의 '기록 저장' 로직으로 넘어가지 않게 종료!
        }

        // 2️⃣ 일반 채팅 처리 루틴 (명령어가 아닐 때만 실행됨)
        // 기록 저장
        chatHistory.add(payload);
        if (chatHistory.size() > 100) {
            chatHistory.remove(0);
        }

        // 모든 접속자에게 브로드캐스트
        broadcast(payload);
    }

    // 헬퍼 메서드: 모든 세션에 메시지 전송 (중복 코드 방지)
    private void broadcast(String msg) throws Exception {
        for (WebSocketSession s : sessionNames.keySet()) {
            if (s.isOpen()) {
                s.sendMessage(new TextMessage(msg));
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
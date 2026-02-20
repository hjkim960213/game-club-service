package com.example.gameclubservice.controller;

import com.example.gameclubservice.domain.Account;
import com.example.gameclubservice.service.AccountService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping("/api/login/guest")
    public String guestLogin(@RequestParam String nickname, HttpServletRequest request) {
        // 기존 세션이 있다면 제거하고 새로 생성하여 닉네임 꼬임 방지
        HttpSession oldSession = request.getSession(false);
        if (oldSession != null) oldSession.invalidate();

        Account account = accountService.loginAsGuest(nickname, request.getRemoteAddr());
        request.getSession(true).setAttribute("user", account);
        return account.getNickname() + "님 환영합니다!";
    }

    @PostMapping("/api/login/admin")
    public String adminLogin(@RequestParam String loginId, @RequestParam String password, HttpServletRequest request) {
        Account account = accountService.loginAsAdmin(loginId, password);
        request.getSession(true).setAttribute("user", account);
        return account.getNickname() + "님(운영진) 환영합니다!";
    }

    @GetMapping("/api/me")
    public Account getMyInfo(HttpSession session) {
        return (Account) session.getAttribute("user");
    }

    @PostMapping("/api/logout")
    public String logout(HttpSession session) {
        if (session != null) {
            session.invalidate(); // 서버 세션을 완전히 파괴하여 게스트 정보 초기화
        }
        return "로그아웃 되었습니다.";
    }
}
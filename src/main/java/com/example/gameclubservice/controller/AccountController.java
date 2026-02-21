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
        HttpSession oldSession = request.getSession(false);
        if (oldSession != null) oldSession.invalidate();

        Account account = accountService.loginAsGuest(nickname, request.getRemoteAddr());
        HttpSession session = request.getSession(true);

        // 🚩 중요: 웹소켓 인터셉터가 이 키값들을 복사해갑니다.
        session.setAttribute("user", account);
        session.setAttribute("nickname", account.getNickname());
        session.setAttribute("role", "GUEST");

        return account.getNickname() + "님 환영합니다!";
    }

    @PostMapping("/api/login/admin")
    public String adminLogin(@RequestParam String loginId, @RequestParam String password, HttpServletRequest request) {
        Account account = accountService.loginAsAdmin(loginId, password);
        HttpSession session = request.getSession(true);

        // 🚩 운영진 권한을 ADMIN으로 확실히 명시합니다.
        session.setAttribute("user", account);
        session.setAttribute("nickname", account.getNickname());
        session.setAttribute("role", "ADMIN");

        return account.getNickname() + "님(운영진) 환영합니다!";
    }

    @GetMapping("/api/me")
    public Account getMyInfo(HttpSession session) {
        return (Account) session.getAttribute("user");
    }

    @PostMapping("/api/logout")
    public String logout(HttpSession session) {
        if (session != null) session.invalidate();
        return "로그아웃 되었습니다.";
    }
}
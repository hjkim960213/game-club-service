package com.example.gameclubservice.service;

import com.example.gameclubservice.domain.Account;
import com.example.gameclubservice.domain.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;

    @Transactional
    public Account loginAsGuest(String nickname, String ip) {
        // 🚩 수정됨: IP가 아닌 '닉네임'으로 검색합니다.
        // 이렇게 해야 같은 와이파이를 써도 닉네임이 다르면 새 계정으로 접속됩니다.
        return accountRepository.findByNickname(nickname)
                .map(existingGuest -> {
                    // 이미 있는 닉네임이면 최신 IP만 업데이트해줍니다.
                    existingGuest.setLastIp(ip);
                    return accountRepository.save(existingGuest);
                })
                .orElseGet(() -> {
                    // 없는 닉네임이면 새로 생성합니다.
                    Account newGuest = new Account();
                    newGuest.setNickname(nickname);
                    newGuest.setRole("GUEST");
                    newGuest.setLastIp(ip);
                    return accountRepository.save(newGuest);
                });
    }

    @Transactional(readOnly = true)
    public Account loginAsAdmin(String loginId, String password) {
        Account admin = accountRepository.findByLoginId(loginId)
                .orElseThrow(() -> new RuntimeException("ID 없음"));
        if (!admin.getPassword().equals(password)) throw new RuntimeException("PW 틀림");
        return admin;
    }
}
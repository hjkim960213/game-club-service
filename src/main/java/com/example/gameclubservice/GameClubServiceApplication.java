package com.example.gameclubservice;

import com.example.gameclubservice.domain.Account;
import com.example.gameclubservice.domain.AccountRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.List;

@SpringBootApplication
public class GameClubServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(GameClubServiceApplication.class, args);
    }

    @Bean
    public CommandLineRunner initAdminAccounts(AccountRepository repository) {
        return args -> {
            // 관리자 계정 정보 리스트 (ID, PW, 닉네임 순서)
            // 여기에 희준님이 생각하신 6개의 계정 정보를 적으시면 됩니다!
            createAdminIfAbsent(repository, "1팀장", "1111", "1팀장");
            createAdminIfAbsent(repository, "2팀장", "2222", "2팀장");
            createAdminIfAbsent(repository, "3팀장", "3333", "3팀장");
            createAdminIfAbsent(repository, "4팀장", "4444", "4팀장");
            createAdminIfAbsent(repository, "5팀장", "5555", "5팀장");
            createAdminIfAbsent(repository, "6팀장", "6666", "6팀장");

            System.out.println("✅ 관리자 계정 체크 및 생성 프로세스 완료");
        };
    }

    /**
     * ID가 존재하지 않을 때만 관리자를 생성하는 헬퍼 메서드
     */
    private void createAdminIfAbsent(AccountRepository repository, String id, String pw, String nick) {
        if (repository.findByLoginId(id).isEmpty()) {
            Account admin = new Account();
            admin.setLoginId(id);
            admin.setPassword(pw);
            admin.setNickname(nick);
            admin.setRole("ADMIN");
            repository.save(admin);
            System.out.println("👉 생성됨: " + nick + "(" + id + ")");
        }
    }
}
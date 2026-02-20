package com.example.gameclubservice.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface AccountRepository extends JpaRepository<Account, UUID> {
    Optional<Account> findByLoginId(String loginId);
    Optional<Account> findByNickname(String nickname);
}
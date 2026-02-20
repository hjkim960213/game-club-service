package com.example.gameclubservice.domain;

// ❌ 기존의 springframework.context.annotation.Profile은 지우세요.
// ✅ 우리가 직접 만든 domain.Profile을 가져와야 합니다.
import com.example.gameclubservice.domain.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface ProfileRepository extends JpaRepository<Profile, UUID> {
}
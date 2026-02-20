package com.example.gameclubservice.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional; // Optional 임포트 추가
import java.util.UUID;

public interface TeamRepository extends JpaRepository<Team, UUID> {

    // 반환 타입을 Team에서 Optional<Team>으로 수정했습니다.
    // 이렇게 해야 Service에서 .orElseThrow()를 사용할 수 있습니다.
    Optional<Team> findByTeamName(String teamName);

    // 승점 기준 내림차순 정렬 (기존 코드 유지)
    List<Team> findAllByOrderByTotalPointsDesc();
}
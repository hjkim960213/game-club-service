package com.example.gameclubservice.controller;

import com.example.gameclubservice.domain.Account;
import com.example.gameclubservice.domain.Match;
import com.example.gameclubservice.domain.MatchRepository;
import com.example.gameclubservice.domain.Team;
import com.example.gameclubservice.domain.TeamRepository;
import com.example.gameclubservice.service.LeagueService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class LeagueController {

    private final LeagueService leagueService;
    private final MatchRepository matchRepository;
    private final TeamRepository teamRepository;

    // 🚩 [핵심 추가됨] 1. 순위표 데이터 불러오기 (승점 높은 순으로 정렬하여 전달)
    @GetMapping("/teams")
    public List<Team> getTeams() {
        return teamRepository.findAll(Sort.by(Sort.Direction.DESC, "totalPoints"));
    }

    // 🚩 [핵심 추가됨] 2. 대진표 데이터 불러오기
    @GetMapping("/matches")
    public List<Match> getMatches() {
        return matchRepository.findAll();
    }

    // 3. 경기 결과 수정 및 반영
    @GetMapping(value = "/update-result", produces = "text/plain;charset=UTF-8")
    public String updateResult(@RequestParam UUID matchId, @RequestParam int homeScore, @RequestParam int awayScore) {
        leagueService.updateMatchResult(matchId, homeScore, awayScore);
        return "경기 결과가 성공적으로 반영되었습니다.";
    }

    // 4. 대진표 신규 생성
    @GetMapping(value = "/generate-schedule", produces = "text/plain;charset=UTF-8")
    public String generateSchedule() {
        return leagueService.generateRoundRobinSchedule();
    }

    // 5. 모든 데이터 완전 초기화
    @GetMapping(value = "/reset-all", produces = "text/plain;charset=UTF-8")
    public String resetAll(HttpSession session) {
        Account user = (Account) session.getAttribute("user");

        if (user == null || !"ADMIN".equals(user.getRole())) {
            return "권한이 없습니다.";
        }

        matchRepository.deleteAll();
        teamRepository.deleteAll(); // 팀 완전히 삭제

        return "모든 대진표와 팀 데이터가 완전히 초기화되었습니다.";
    }

    // 6. 팀 생성
    @GetMapping(value = "/create-team", produces = "text/plain;charset=UTF-8")
    public String createTeam(@RequestParam String name) {
        return leagueService.createNewTeam(name);
    }
}
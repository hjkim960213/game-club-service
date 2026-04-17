package com.example.gameclubservice.service;

import com.example.gameclubservice.domain.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LeagueService {

    private final TeamRepository teamRepository;
    private final MatchRepository matchRepository;

    @Transactional
    public String createNewTeam(String teamName) {
        if (teamRepository.findByTeamName(teamName).isPresent()) {
            return "실패: 이미 존재하는 팀 이름입니다.";
        }

        Team team = new Team();
        team.setTeamName(teamName);
        team.setWins(0);
        team.setDraws(0);
        team.setLosses(0);
        team.setTotalPoints(0);
        // [추가됨] 초기 득실점 세팅
        team.setGoalsFor(0);
        team.setGoalsAgainst(0);

        teamRepository.save(team);
        return teamName + " 팀이 생성되었습니다.";
    }

    @Transactional
    public void updateMatchResult(UUID matchId, int homeScore, int awayScore) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException("해당 경기를 찾을 수 없습니다."));

        if ("FINISHED".equals(match.getStatus())) {
            revertPoints(match);
        }

        match.setHomeScore(homeScore);
        match.setAwayScore(awayScore);
        match.setStatus("FINISHED");

        Team home = teamRepository.findByTeamName(match.getHomeTeamName())
                .orElseThrow(() -> new RuntimeException("홈 팀을 찾을 수 없습니다."));
        Team away = teamRepository.findByTeamName(match.getAwayTeamName())
                .orElseThrow(() -> new RuntimeException("어웨이 팀을 찾을 수 없습니다."));

        // [추가됨] 승무패 상관없이 이번 경기의 득점/실점 누적
        home.setGoalsFor(home.getGoalsFor() + homeScore);
        home.setGoalsAgainst(home.getGoalsAgainst() + awayScore);
        away.setGoalsFor(away.getGoalsFor() + awayScore);
        away.setGoalsAgainst(away.getGoalsAgainst() + homeScore);

        // 승패에 따른 기록 업데이트
        if (homeScore > awayScore) {
            home.setWins(home.getWins() + 1); home.setTotalPoints(home.getTotalPoints() + 3);
            away.setLosses(away.getLosses() + 1);
        } else if (homeScore < awayScore) {
            away.setWins(away.getWins() + 1); away.setTotalPoints(away.getTotalPoints() + 3);
            home.setLosses(home.getLosses() + 1);
        } else {
            home.setDraws(home.getDraws() + 1); home.setTotalPoints(home.getTotalPoints() + 1);
            away.setDraws(away.getDraws() + 1); away.setTotalPoints(away.getTotalPoints() + 1);
        }

        matchRepository.save(match);
        teamRepository.save(home);
        teamRepository.save(away);
    }

    private void revertPoints(Match match) {
        Team home = teamRepository.findByTeamName(match.getHomeTeamName())
                .orElseThrow(() -> new RuntimeException("홈 팀을 찾을 수 없습니다."));
        Team away = teamRepository.findByTeamName(match.getAwayTeamName())
                .orElseThrow(() -> new RuntimeException("어웨이 팀을 찾을 수 없습니다."));

        // [추가됨] 기존 경기 기록 롤백 시 득점/실점도 같이 차감
        home.setGoalsFor(home.getGoalsFor() - match.getHomeScore());
        home.setGoalsAgainst(home.getGoalsAgainst() - match.getAwayScore());
        away.setGoalsFor(away.getGoalsFor() - match.getAwayScore());
        away.setGoalsAgainst(away.getGoalsAgainst() - match.getHomeScore());

        if (match.getHomeScore() > match.getAwayScore()) {
            home.setWins(home.getWins() - 1); home.setTotalPoints(home.getTotalPoints() - 3);
            away.setLosses(away.getLosses() - 1);
        } else if (match.getHomeScore() < match.getAwayScore()) {
            away.setWins(away.getWins() - 1); away.setTotalPoints(away.getTotalPoints() - 3);
            home.setLosses(home.getLosses() - 1);
        } else {
            home.setDraws(home.getDraws() - 1); home.setTotalPoints(home.getTotalPoints() - 1);
            away.setDraws(away.getDraws() - 1); away.setTotalPoints(away.getTotalPoints() - 1);
        }
    }

    @Transactional
    public String generateRoundRobinSchedule() {
        matchRepository.deleteAll();
        List<Team> teams = teamRepository.findAll();
        if (teams.size() < 2) return "실패: 팀이 최소 2개 이상 필요합니다.";

        for (int i = 0; i < teams.size(); i++) {
            for (int j = i + 1; j < teams.size(); j++) {
                Match match = new Match();
                match.setHomeTeamName(teams.get(i).getTeamName());
                match.setAwayTeamName(teams.get(j).getTeamName());
                match.setHomeScore(0);
                match.setAwayScore(0);
                match.setStatus("SCHEDULED");
                matchRepository.save(match);
            }
        }
        return "대진표가 생성되었습니다.";
    }

    @Transactional
    public String resetAllData() {
        matchRepository.deleteAll();
        teamRepository.deleteAll();
        return "모든 데이터가 완전히 초기화되었습니다.";
    }
}
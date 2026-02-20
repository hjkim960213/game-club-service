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

    /**
     * [추가됨] 새로운 팀을 생성합니다.
     */
    @Transactional
    public String createNewTeam(String teamName) {
        // 이미 존재하는 팀 이름인지 확인
        if (teamRepository.findByTeamName(teamName).isPresent()) {
            return "실패: 이미 존재하는 팀 이름입니다.";
        }

        Team team = new Team();
        team.setTeamName(teamName);
        team.setWins(0);
        team.setDraws(0);
        team.setLosses(0);
        team.setTotalPoints(0);

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

    /**
     * 모든 경기와 팀 데이터를 완전히 삭제합니다.
     */
    @Transactional
    public String resetAllData() {
        // 1. 모든 경기 기록 삭제
        matchRepository.deleteAll();

        // 2. 모든 팀 데이터 삭제 (전적 초기화가 아니라 아예 삭제하여 '이미 있는 팀' 오류 방지)
        teamRepository.deleteAll();

        return "모든 데이터가 완전히 초기화되었습니다.";
    }










}
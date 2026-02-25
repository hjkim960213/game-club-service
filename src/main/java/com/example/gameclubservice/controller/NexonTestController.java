package com.example.gameclubservice.controller;

import com.example.gameclubservice.dto.MatchResponseDto;
import com.example.gameclubservice.service.NexonApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class NexonTestController {

    private final NexonApiService nexonApiService;

    @GetMapping("/api/test/ouid")
    public String testGetOuid(@RequestParam String nickname) {
        String ouid = nexonApiService.getOuid(nickname);
        return ouid != null ? "✅ 성공! [" + nickname + "]님의 OUID는: " + ouid + " 입니다." : "❌ 실패! API 키나 닉네임을 확인해주세요.";
    }

    @GetMapping("/api/test/matches")
    public List<String> testGetMatches(@RequestParam String nickname) {
        String ouid = nexonApiService.getOuid(nickname);
        if (ouid == null) return java.util.Collections.singletonList("❌ OUID를 찾을 수 없습니다.");
        return nexonApiService.getVoltaMatchIds(ouid);
    }

    @GetMapping(value = "/api/test/match-detail", produces = "text/html;charset=UTF-8")
    public String testGetMatchDetail(@RequestParam String matchId) {

        MatchResponseDto detail = nexonApiService.getMatchDetail(matchId);

        if (detail == null || detail.getMatchInfo() == null) {
            return "<h3>❌ 경기 정보를 불러오지 못했습니다. 매치 ID를 확인해주세요.</h3>";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("<h2>⚽ 매치 상세 스탯 결과 (매치번호: ").append(matchId).append(")</h2><hr>");

        for (MatchResponseDto.MatchInfo info : detail.getMatchInfo()) {
            sb.append("<h3>👤 유저명: <span style='color:blue'>").append(info.getNickname()).append("</span> ");

            String result = (info.getMatchDetail() != null && info.getMatchDetail().getMatchResult() != null)
                    ? info.getMatchDetail().getMatchResult()
                    : "기록 없음";
            sb.append("(결과: ").append(result).append(")</h3>");

            if (info.getPlayer() != null) {
                for (MatchResponseDto.Player p : info.getPlayer()) {
                    if (p.getStatus() != null && p.getStatus().getSpRating() != null && p.getStatus().getSpRating() > 0) {
                        MatchResponseDto.Status s = p.getStatus();

                        // 💡 여기서 진짜 이름표로 꺼냅니다! (null이면 0으로 처리)
                        Integer tSuccess = s.getTackle() != null ? s.getTackle() : 0;
                        Integer bSuccess = s.getBlock() != null ? s.getBlock() : 0;
                        Integer iSuccess = s.getIntercept() != null ? s.getIntercept() : 0;

                        sb.append("<ul>")
                                .append("<li>선수 고유번호: ").append(p.getSpId()).append("</li>")
                                .append("<li>⭐ 평점: <b style='color:red'>").append(s.getSpRating()).append("</b></li>")
                                .append("<li>⚽ 공격: 골 ").append(s.getGoal()).append(" / 어시스트 ").append(s.getAssist()).append("</li>")
                                .append("<li>🎯 슈팅: ").append(s.getShoot()).append(" (유효슈팅: ").append(s.getEffectiveShoot()).append(")</li>")
                                .append("<li>🔄 패스: ").append(s.getPassSuccess()).append(" 성공 / ").append(s.getPassTry()).append(" 시도</li>")
                                .append("<li>🛡️ 수비: 태클 성공 ").append(tSuccess).append(" / 블로킹 성공 ").append(bSuccess).append(" / 가로채기 ").append(iSuccess).append("</li>")
                                .append("</ul>");
                    }
                }
            }
        }
        return sb.toString();
    }

    // ==========================================
    // 🚑 긴급 디버깅용 API: 화면에 넥슨 JSON 데이터 통째로 뿌리기
    // ==========================================
    @GetMapping(value = "/api/test/match-raw", produces = "application/json;charset=UTF-8")
    public String testGetMatchRaw(@RequestParam String matchId) {
        return nexonApiService.getRawMatchDetailString(matchId);
    }


}
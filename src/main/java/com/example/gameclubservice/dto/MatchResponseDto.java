package com.example.gameclubservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class MatchResponseDto {
    private List<MatchInfo> matchInfo;

    @Getter @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MatchInfo {
        private String nickname;
        private MatchDetail matchDetail;
        private List<Player> player;
    }

    @Getter @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MatchDetail {
        private String matchResult;
    }

    @Getter @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Player {
        private Integer spId;      // 💡 int -> Integer 로 변경
        private Integer spPosition; // 💡 int -> Integer 로 변경
        private Status status;
    }

    @Getter @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Status {
        private Float spRating;
        private Integer goal;
        private Integer assist;
        private Integer shoot;
        private Integer effectiveShoot;
        private Integer passTry;
        private Integer passSuccess;
        private Integer dribbleSuccess;

        // 🔥 넥슨 API 진짜 공식 이름표로 복구! 🔥
        private Integer tackleTry;      // 태클 시도
        private Integer tackle;         // 태클 성공 (이게 찐입니다!)
        private Integer blockTry;       // 차단 시도
        private Integer block;          // 차단 성공
        private Integer intercept;      // 가로채기

        private Integer yellowCards;
        private Integer redCards;
    }
}
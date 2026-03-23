package com.example.gameclubservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class MatchResponseDto {

    // 💡 바로 여기에 추가합니다! (경기 날짜 정보를 통째로 받음)
    private String matchDate;

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

        // 💡 (참고) 아까 프론트엔드에서 무승부 찢을 때 썼던 세부 스탯들도 받아오려면 여기 있어야 합니다!
        private Integer possession;
        private Integer foul;
        private Integer cornerKick;
        private Integer OffsideCount;
    }

    @Getter @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Player {
        private Integer spId;
        private Integer spPosition;
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

        private Integer tackleTry;
        private Integer tackle;
        private Integer blockTry;
        private Integer block;
        private Integer intercept;

        private Integer yellowCards;
        private Integer redCards;
    }
}
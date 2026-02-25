package com.example.gameclubservice.controller;

import com.example.gameclubservice.dto.MatchResponseDto;
import com.example.gameclubservice.service.NexonApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/volta")
@RequiredArgsConstructor
public class VoltaApiController {

    private final NexonApiService nexonApiService;

    @GetMapping("/recent")
    public ResponseEntity<?> getRecentMatches(@RequestParam String nickname) {

        // 🚀 수정됨: 단건이 아니라 List<MatchResponseDto> 로 받아옵니다.
        List<MatchResponseDto> matches = nexonApiService.getRecentVoltaMatchesByNickname(nickname);

        if (matches == null || matches.isEmpty()) {
            return ResponseEntity.status(404).body("최근 볼타 경기 기록을 찾을 수 없습니다.");
        }

        return ResponseEntity.ok(matches); // 프론트엔드에 5경기 데이터를 배열로 쏴줍니다!
    }
}
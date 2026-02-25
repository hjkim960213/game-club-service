package com.example.gameclubservice.service;

import com.example.gameclubservice.dto.NexonUserDto;
import com.example.gameclubservice.dto.MatchResponseDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class NexonApiService {

    @Value("${nexon.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final String BASE_URL = "https://open.api.nexon.com/fconline/v1";

    public String getOuid(String nickname) {
        try {
            URI uri = UriComponentsBuilder.fromUriString(BASE_URL + "/id").queryParam("nickname", nickname).build().encode().toUri();
            HttpHeaders headers = new HttpHeaders(); headers.set("x-nxopen-api-key", apiKey);
            ResponseEntity<NexonUserDto> response = restTemplate.exchange(uri, HttpMethod.GET, new HttpEntity<>(headers), NexonUserDto.class);
            if (response.getBody() != null) return response.getBody().getOuid();
        } catch (Exception e) { System.out.println("❌ OUID 조회 실패: " + e.getMessage()); }
        return null;
    }

    public List<String> getVoltaMatchIds(String ouid) {
        try {
            URI uri = UriComponentsBuilder.fromUriString(BASE_URL + "/user/match").queryParam("ouid", ouid)
                    .queryParam("matchtype", 214).queryParam("offset", 0).queryParam("limit", 20).build().toUri();
            HttpHeaders headers = new HttpHeaders(); headers.set("x-nxopen-api-key", apiKey);
            ResponseEntity<List> response = restTemplate.exchange(uri, HttpMethod.GET, new HttpEntity<>(headers), List.class);
            if (response.getBody() != null) return response.getBody();
        } catch (Exception e) { System.out.println("❌ 매치 ID 조회 실패: " + e.getMessage()); }
        return Collections.emptyList();
    }

    public MatchResponseDto getMatchDetail(String matchId) {
        try {
            URI uri = UriComponentsBuilder.fromUriString(BASE_URL + "/match-detail").queryParam("matchid", matchId).build().toUri();
            HttpHeaders headers = new HttpHeaders(); headers.set("x-nxopen-api-key", apiKey);
            ResponseEntity<MatchResponseDto> response = restTemplate.exchange(uri, HttpMethod.GET, new HttpEntity<>(headers), MatchResponseDto.class);
            if (response.getBody() != null) return response.getBody();
        } catch (Exception e) { System.out.println("❌ 매치 스탯 조회 실패: " + e.getMessage()); }
        return null;
    }

    // 🚀 수정됨: 최근 1경기가 아니라 '최근 5경기' 전체 스탯을 List에 담아서 리턴합니다!
    public List<MatchResponseDto> getRecentVoltaMatchesByNickname(String nickname) {
        String ouid = getOuid(nickname);
        if (ouid == null) return null;

        List<String> matchIds = getVoltaMatchIds(ouid);
        if (matchIds == null || matchIds.isEmpty()) return null;

        List<MatchResponseDto> result = new ArrayList<>();
        // 💡 넥슨 통신 속도 고려하여 최근 5경기만 긁어옵니다. (늘리고 싶으시면 숫자를 바꾸세요!)
        int limit = Math.min(matchIds.size(), 20);
        for (int i = 0; i < limit; i++) {
            MatchResponseDto detail = getMatchDetail(matchIds.get(i));
            if (detail != null) {
                result.add(detail);
            }
        }
        return result;
    }

    // ==========================================
    // 🚑 긴급 디버깅용: 넥슨이 주는 날것(Raw) JSON 통째로 뽑아오기 (다시 추가 완료!)
    // ==========================================
    public String getRawMatchDetailString(String matchId) {
        try {
            URI uri = UriComponentsBuilder
                    .fromUriString(BASE_URL + "/match-detail")
                    .queryParam("matchid", matchId)
                    .build()
                    .toUri();

            HttpHeaders headers = new HttpHeaders();
            headers.set("x-nxopen-api-key", apiKey);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    uri,
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            return response.getBody();
        } catch (Exception e) {
            return "❌ 날것의 데이터 조회 실패: " + e.getMessage();
        }
    }
}
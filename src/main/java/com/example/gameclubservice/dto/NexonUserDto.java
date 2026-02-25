package com.example.gameclubservice.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NexonUserDto {
    // 넥슨이 주는 JSON의 키 이름("ouid")과 스펠링이 똑같아야 알아서 쏙 들어갑니다!
    private String ouid;
}
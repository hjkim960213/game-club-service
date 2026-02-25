package com.example.gameclubservice.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClubMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 멤버 고유 번호

    private String nickname; // 닉네임
    private String position; // 주 포지션 (예: ST, CAM, CB 등)
    private String description; // 멤버 한줄평 소개
}
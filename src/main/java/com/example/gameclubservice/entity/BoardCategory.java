package com.example.gameclubservice.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoardCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name; // 탭 이름 (예: 공지사항, 플레이팁, 자유게시판)

    // 💡 공지사항처럼 '운영진만 글을 쓸 수 있는 탭'인지 구분하는 스위치!
    private boolean adminOnlyWrite;
}
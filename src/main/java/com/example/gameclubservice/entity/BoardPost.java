package com.example.gameclubservice.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoardPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 💡 이 글이 어느 카테고리(탭)에 속해 있는지 연결해주는 핵심 코드!
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private BoardCategory category;

    private String title; // 글 제목

    @Column(columnDefinition = "TEXT")
    private String content; // 글 내용 (길 수 있으므로 TEXT 타입 지정)

    private String authorNickname; // 작성자 닉네임

    private LocalDateTime createdAt; // 작성 시간

    // DB에 저장되기 직전에 현재 시간을 자동으로 찍어주는 마법의 어노테이션
    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
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

    private String name;
    private boolean adminOnlyWrite;

    // 💡 탭 순서를 기억할 변수 추가!
    @Column(columnDefinition = "int default 0")
    private Integer displayOrder;
}
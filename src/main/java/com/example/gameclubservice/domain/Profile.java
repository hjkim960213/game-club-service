package com.example.gameclubservice.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.UUID;

@Entity // 이 클래스가 데이터베이스 테이블과 매핑됨을 의미합니다.
@Table(name = "profiles") // 실제 Supabase에 만든 테이블 이름입니다.
@Getter @Setter // Lombok을 사용하여 Getter와 Setter를 자동으로 생성합니다.
public class Profile {

    @Id // 기본키(Primary Key)임을 나타냅니다.
    private UUID id;

    @Column(name = "login_id") // DB의 login_id 컬럼과 연결합니다.
    private String loginId;

    private String nickname;

    private String position;

    private String role;

    // Profile 클래스 내부 적절한 곳에 추가
    @ManyToOne
    @JoinColumn(name = "team_id")
    private Team team;

}
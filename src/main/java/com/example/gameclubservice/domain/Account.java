package com.example.gameclubservice.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.UUID;

@Entity
@Getter @Setter
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(unique = true)
    private String loginId; // 관리자 ID
    private String password; // 관리자 PW
    private String nickname; // 닉네임
    private String role;     // ADMIN or GUEST
    private String lastIp;   // 중복 IP 체크용
}
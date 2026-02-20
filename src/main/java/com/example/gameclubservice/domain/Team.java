package com.example.gameclubservice.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "teams")
@Getter @Setter
public class Team {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String teamName;

    private int wins = 0;   // 승
    private int draws = 0;  // 무
    private int losses = 0; // 패
    private int totalPoints = 0; // 승점

    @OneToMany(mappedBy = "team")
    private List<Profile> members = new ArrayList<>();
}
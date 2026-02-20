package com.example.gameclubservice.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "matches")
@Getter @Setter
public class Match {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private String homeTeamName;
    private String awayTeamName;
    private String status; // "SCHEDULED", "FINISHED" 등
    private LocalDateTime matchDate;

    private int homeScore = 0;
    private int awayScore = 0;
}
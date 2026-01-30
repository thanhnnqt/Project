package org.example.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "game_types")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class GameType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name; // e.g., "Tiến Lên", "Poker", "Phỏm"

    private Integer minPlayers;
    private Integer maxPlayers;

    @Column(columnDefinition = "TEXT")
    private String rulesJson; // Detailed rules in JSON format

    private String icon;
}

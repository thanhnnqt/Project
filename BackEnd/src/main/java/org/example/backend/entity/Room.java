package org.example.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "rooms")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String password;

    @ManyToOne
    @JoinColumn(name = "game_type_id")
    private GameType gameType;

    @Column(precision = 19, scale = 2)
    private BigDecimal minBet;

    private String status; // WAITING, PLAYING, CLOSED

    @Column(updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @ManyToOne
    @JoinColumn(name = "host_id")
    private Player host;

    @Column(name = "max_players")
    private Integer maxPlayers;
}

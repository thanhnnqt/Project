package org.example.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "match_history")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class MatchHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "room_id")
    private Room room;

    @ManyToOne
    @JoinColumn(name = "game_type_id")
    private GameType gameType;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    @Column(columnDefinition = "TEXT")
    private String matchLog; // JSON or text representation of match events

    @OneToMany(mappedBy = "matchHistory", cascade = CascadeType.ALL)
    private List<MatchDetail> details;
}

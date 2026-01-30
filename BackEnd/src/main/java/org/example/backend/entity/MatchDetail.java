package org.example.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;

@Entity
@Table(name = "match_details")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class MatchDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "match_history_id")
    private MatchHistory matchHistory;

    @ManyToOne
    @JoinColumn(name = "player_id")
    private Player player;

    private Integer rank; // 1st, 2nd, etc.

    @Column(precision = 19, scale = 2)
    private BigDecimal coinDelta; // Positive for win, negative for loss

    @Column(columnDefinition = "TEXT")
    private String endHand; // The cards player had at the end
}

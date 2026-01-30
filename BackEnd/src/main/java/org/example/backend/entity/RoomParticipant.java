package org.example.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "room_participants")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class RoomParticipant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "room_id")
    private Room room;

    @ManyToOne
    @JoinColumn(name = "player_id")
    private Player player;

    private Integer seatIndex; // Position at the table

    private Boolean isReady = false;

    private LocalDateTime joinedAt = LocalDateTime.now();
}

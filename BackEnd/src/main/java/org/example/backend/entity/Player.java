package org.example.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "players")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Player {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    private String displayName;
    
    private String avatar;

    @Column(nullable = false, unique = true)
    private String email;

    private Integer age;

    private String phoneNumber;

    @Column(precision = 19, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    private String status; // ONLINE, OFFLINE, PLAYING

    private Integer rankPoints = 0;

    private String rankTier = "Sắt IV";

    private LocalDateTime lastLogin;

    @Column(updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}

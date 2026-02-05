package org.example.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;

@Entity
@Table(name = "shop_items")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ShopItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String type; // AVATAR_FRAME, CARD_SKIN

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal price;

    private String imageUrl;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String rarity; // COMMON, RARE, EPIC, LEGENDARY

    private Integer minRankPoints = 0;
}

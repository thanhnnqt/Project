package org.example.backend.game;

import lombok.Getter;

@Getter
public enum Suit {
    SPADE(1, "Bích", "♠"),
    CLUB(2, "Chuồn", "♣"),
    DIAMOND(3, "Rô", "♦"),
    HEART(4, "Cơ", "♥");

    private final int value;
    private final String displayName;
    private final String symbol;

    Suit(int value, String displayName, String symbol) {
        this.value = value;
        this.displayName = displayName;
        this.symbol = symbol;
    }
}

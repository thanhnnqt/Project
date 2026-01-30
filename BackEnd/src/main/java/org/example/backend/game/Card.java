package org.example.backend.game;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Card {
    private Rank rank;
    private Suit suit;

    public String getShortName() {
        return rank.getDisplayName() + suit.getSymbol();
    }

    @Override
    public String toString() {
        return getShortName();
    }
    
    // Helper to get raw value for sorting/comparing in Tien Len
    public int getTienLenValue() {
        return rank.getValue() * 10 + suit.getValue();
    }
}

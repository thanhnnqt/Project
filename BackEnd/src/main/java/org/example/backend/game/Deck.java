package org.example.backend.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Deck {
    private final List<Card> cards;

    public Deck() {
        cards = new ArrayList<>();
        for (Rank rank : Rank.values()) {
            for (Suit suit : Suit.values()) {
                cards.add(new Card(rank, suit));
            }
        }
    }

    public void shuffle() {
        Collections.shuffle(cards);
    }

    public List<List<Card>> deal(int numPlayers, int cardsPerPlayer) {
        List<List<Card>> hands = new ArrayList<>();
        for (int i = 0; i < numPlayers; i++) {
            hands.add(new ArrayList<>());
        }

        int cardIndex = 0;
        for (int i = 0; i < cardsPerPlayer; i++) {
            for (int j = 0; j < numPlayers; j++) {
                if (cardIndex < cards.size()) {
                    hands.get(j).add(cards.get(cardIndex++));
                }
            }
        }
        return hands;
    }
    
    public List<Card> getCards() {
        return new ArrayList<>(cards);
    }
}

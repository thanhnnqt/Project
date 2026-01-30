package org.example.backend.game;

import java.util.List;

public interface IGameLogic {
    /**
     * Checks if a move is valid given the current state of the table.
     */
    boolean isValidMove(List<Card> cardsToPlay, List<Card> lastPlayedCards);

    /**
     * Compares two sets of cards (e.g., to see if one "chặt" the other).
     * Returns > 0 if newCards is stronger than oldCards.
     */
    int compareMoves(List<Card> newCards, List<Card> oldCards);
    
    /**
     * Sorts cards according to game rules.
     */
    void sortCards(List<Card> cards);
}

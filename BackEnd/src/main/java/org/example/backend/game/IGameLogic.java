package org.example.backend.game;

import java.util.List;

public interface IGameLogic {
    /**
     * Handles a player action and updates the game state appropriately.
     * Returns true if the action was valid and processed.
     */
    boolean handleAction(GameState state, GameAction action);

    /**
     * Sets up the initial state for a new game (e.g., dealing cards).
     */
    void setupGame(GameState state);

    /**
     * Determines the winner or calculates final scores.
     */
    void checkWin(GameState state);

    /**
     * Sorts cards according to game rules.
     */
    void sortCards(List<Card> cards);
}

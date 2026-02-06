package org.example.backend.game;

import java.util.*;

public class BinhLogic implements IGameLogic {

    @Override
    public void sortCards(List<Card> cards) {
        // Standard sorting for building hands
        cards.sort(Comparator.comparingInt((Card c) -> c.getRank().getValue())
                .thenComparingInt(c -> c.getSuit().getValue()));
    }

    @Override
    public void setupGame(GameState state) {
        Deck deck = new Deck();
        deck.shuffle();
        
        int playerCount = state.getPlayerIds().size();
        for (int i = 0; i < playerCount; i++) {
            Long playerId = state.getPlayerIds().get(i);
            List<Card> hand = deck.deal(1, 13).get(0);
            sortCards(hand);
            state.getHands().put(playerId, hand);
        }

        state.setGameStarted(true);
        state.setTableCards(new ArrayList<>());
        state.setWinnerId(null);
        state.setGameType("Mậu Binh");
        
        Map<String, Object> binhData = new HashMap<>();
        binhData.put("layouts", new HashMap<Long, List<List<Card>>>());
        binhData.put("readyPlayers", new HashSet<Long>());
        state.setGameData(binhData);
    }

    @Override
    public boolean handleAction(GameState state, GameAction action) {
        if (!state.isGameStarted()) return false;
        
        if (action.getType() == GameAction.ActionType.LAYOUT) {
            return handleLayout(state, action.getPlayerId(), action.getLayout());
        }
        return false;
    }

    private boolean handleLayout(GameState state, Long playerId, List<List<Card>> layout) {
        if (layout == null || layout.size() != 3) return false;
        
        // Validate layout sizes: 5, 5, 3
        if (layout.get(0).size() != 5 || layout.get(1).size() != 5 || layout.get(2).size() != 3) return false;

        Map<String, Object> binhData = state.getGameData();
        Map<Long, List<List<Card>>> layouts = (Map<Long, List<List<Card>>>) binhData.get("layouts");
        Set<Long> readyPlayers = (Set<Long>) binhData.get("readyPlayers");

        layouts.put(playerId, layout);
        readyPlayers.add(playerId);

        // If everyone is ready, compare hands and end game
        if (readyPlayers.size() == state.getPlayerIds().size()) {
            calculateWinner(state);
        }
        return true;
    }

    private void calculateWinner(GameState state) {
        // Complex comparison of all 3 hands across all players
        // For now, pick first player as placeholder
        state.setWinnerId(state.getPlayerIds().get(0));
        state.setGameStarted(false);
    }

    @Override
    public void checkWin(GameState state) {
        // Handled in calculateWinner
    }
}

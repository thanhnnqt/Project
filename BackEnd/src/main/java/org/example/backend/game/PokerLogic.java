package org.example.backend.game;

import java.util.*;

public class PokerLogic implements IGameLogic {

    @Override
    public void sortCards(List<Card> cards) {
        // ACE is usually high in Poker (14)
        cards.sort(Comparator.comparingInt((Card c) -> {
            int val = c.getRank().getValue();
            return (val == 15) ? 2 : val; // In Rank enum, TWO=15, ACE=14. Normalize for Poker.
        }).reversed());
    }

    @Override
    public void setupGame(GameState state) {
        Deck deck = new Deck();
        deck.shuffle();
        
        int playerCount = state.getPlayerIds().size();
        for (int i = 0; i < playerCount; i++) {
            Long playerId = state.getPlayerIds().get(i);
            List<Card> hand = deck.deal(1, 2).get(0);
            state.getHands().put(playerId, hand);
        }

        state.setGameStarted(true);
        state.setTableCards(new ArrayList<>()); // Community cards
        state.setWinnerId(null);
        state.setGameType("Poker");
        
        Map<String, Object> pokerData = new HashMap<>();
        pokerData.put("deck", deck);
        pokerData.put("pot", 0);
        pokerData.put("currentBet", 0);
        pokerData.put("stage", "PRE_FLOP"); // PRE_FLOP, FLOP, TURN, RIVER, SHOWDOWN
        pokerData.put("playerBets", new HashMap<Long, Integer>());
        pokerData.put("foldedPlayers", new HashSet<Long>());
        state.setGameData(pokerData);
    }

    @Override
    public boolean handleAction(GameState state, GameAction action) {
        if (!state.isGameStarted()) return false;
        
        Map<String, Object> pokerData = state.getGameData();
        Set<Long> folded = (Set<Long>) pokerData.get("foldedPlayers");
        if (folded.contains(action.getPlayerId())) return false;

        switch (action.getType()) {
            case CHECK:
                return handleCheck(state, action.getPlayerId());
            case CALL:
                return handleCall(state, action.getPlayerId());
            case RAISE:
            case BET:
                return handleBet(state, action.getPlayerId(), action.getAmount());
            case FOLD:
                return handleFold(state, action.getPlayerId());
            default:
                return false;
        }
    }

    private boolean handleCheck(GameState state, Long playerId) {
        // Logic for checking
        state.nextTurn();
        checkStageTransition(state);
        return true;
    }

    private boolean handleCall(GameState state, Long playerId) {
        // Logic for calling current bet
        state.nextTurn();
        checkStageTransition(state);
        return true;
    }

    private boolean handleBet(GameState state, Long playerId, Integer amount) {
        if (amount == null || amount <= 0) return false;
        // Logic for betting/raising
        state.nextTurn();
        return true;
    }

    private boolean handleFold(GameState state, Long playerId) {
        Map<String, Object> pokerData = state.getGameData();
        Set<Long> folded = (Set<Long>) pokerData.get("foldedPlayers");
        folded.add(playerId);
        
        if (folded.size() == state.getPlayerIds().size() - 1) {
            // One player left, they win
            state.setWinnerId(state.getPlayerIds().stream().filter(id -> !folded.contains(id)).findFirst().orElse(null));
            state.setGameStarted(false);
        } else {
            state.nextTurn();
        }
        return true;
    }

    private void checkStageTransition(GameState state) {
        // Logic to move from Pre-flop to Flop, etc.
        // If all players have acted and bets are equal
    }

    @Override
    public void checkWin(GameState state) {
        // Evaluate best 5-card hands
    }
}

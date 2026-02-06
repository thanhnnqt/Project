package org.example.backend.game;

import java.util.*;

public class PhomLogic implements IGameLogic {

    @Override
    public void sortCards(List<Card> cards) {
        // Phom sorts by rank then suit
        cards.sort(Comparator.comparingInt((Card c) -> c.getRank().getValue())
                .thenComparingInt(c -> c.getSuit().getValue()));
    }

    @Override
    public void setupGame(GameState state) {
        Deck deck = new Deck();
        deck.shuffle();
        
        // 9 cards per player, except host gets 10
        int playerCount = state.getPlayerIds().size();
        for (int i = 0; i < playerCount; i++) {
            Long playerId = state.getPlayerIds().get(i);
            int cardsToDeal = (playerId.equals(state.getHostId())) ? 10 : 9;
            List<Card> hand = new ArrayList<>();
            for (int j = 0; j < cardsToDeal; j++) {
                hand.add(deck.draw());
            }
            sortCards(hand);
            state.getHands().put(playerId, hand);
        }

        state.setGameStarted(true);
        state.setTableCards(new ArrayList<>()); // Used as trash pile in Phom
        state.getPassedPlayers().clear();
        state.setWinnerId(null);
        state.setCurrentTurnIndex(state.getPlayerIds().indexOf(state.getHostId()));
        state.setGameType("Phỏm");
        
        // Initialize Phom-specific state
        Map<String, Object> phomData = new HashMap<>();
        phomData.put("deck", deck);
        phomData.put("trashPile", new ArrayList<Card>()); // Shared legacy, but we'll use individual ones too
        phomData.put("playerTrashPiles", new HashMap<Long, List<Card>>()); // NEW: Individual discards
        phomData.put("stolenCards", new HashMap<Long, List<Card>>()); // NEW: Track eaten cards
        phomData.put("melds", new HashMap<Long, List<List<Card>>>());
        phomData.put("round", 1); // Track rounds for "Chốt"
        phomData.put("turnStage", "DISCARD"); // Host starts with 10 cards, so they discard first
        
        // Initialize maps for all players
        for (Long id : state.getPlayerIds()) {
            ((Map<Long, List<Card>>) phomData.get("playerTrashPiles")).put(id, new ArrayList<>());
            ((Map<Long, List<Card>>) phomData.get("stolenCards")).put(id, new ArrayList<>());
        }
        
        state.setGameData(phomData);
    }

    @Override
    public boolean handleAction(GameState state, GameAction action) {
        if (!state.isGameStarted()) return false;
        if (!state.getCurrentPlayerId().equals(action.getPlayerId())) return false;

        Map<String, Object> phomData = state.getGameData();
        String stage = (String) phomData.get("turnStage");

        switch (action.getType()) {
            case DRAW:
                if (!"DRAW".equals(stage)) return false;
                return handleDraw(state, action.getPlayerId());
            case STEAL:
                if (!"DRAW".equals(stage)) return false;
                return handleSteal(state, action.getPlayerId());
            case PLAY: // Used as DISCARD in Phom
                if (!"DISCARD".equals(stage)) return false;
                return handleDiscard(state, action.getPlayerId(), action.getCards());
            case MELD: // "Hạ bài"
                return handleMeld(state, action.getPlayerId(), action.getLayout());
            case ATTACH: // "Gửi bài"
                return handleAttach(state, action.getPlayerId(), action.getCards(), action.getTargetId());
            default:
                return false;
        }
    }

    private boolean handleDraw(GameState state, Long playerId) {
        Map<String, Object> phomData = state.getGameData();
        Deck deck = (Deck) phomData.get("deck");
        
        Card drawn = deck.draw();
        if (drawn == null) {
            checkWin(state);
            return true;
        }

        state.getHands().get(playerId).add(drawn);
        sortCards(state.getHands().get(playerId));
        phomData.put("turnStage", "DISCARD");
        return true;
    }

    private boolean handleSteal(GameState state, Long playerId) {
        Map<String, Object> phomData = state.getGameData();
        List<Card> sharedTrash = (List<Card>) phomData.get("trashPile");
        if (sharedTrash.isEmpty()) return false;

        Card lastDiscarded = sharedTrash.remove(sharedTrash.size() - 1);
        
        // Remove from the previous player's individual trash pile too
        int prevIdx = (state.getPlayerIds().indexOf(playerId) - 1 + state.getPlayerIds().size()) % state.getPlayerIds().size();
        Long prevPlayerId = state.getPlayerIds().get(prevIdx);
        Map<Long, List<Card>> playerTrashes = (Map<Long, List<Card>>) phomData.get("playerTrashPiles");
        if (playerTrashes.containsKey(prevPlayerId)) {
            List<Card> prevTrash = playerTrashes.get(prevPlayerId);
            if (!prevTrash.isEmpty()) prevTrash.remove(prevTrash.size() - 1);
        }

        // Add to current player's stolen cards and hand
        state.getHands().get(playerId).add(lastDiscarded);
        Map<Long, List<Card>> stolen = (Map<Long, List<Card>>) phomData.get("stolenCards");
        stolen.get(playerId).add(lastDiscarded);
        
        sortCards(state.getHands().get(playerId));
        phomData.put("turnStage", "DISCARD");
        return true;
    }

    private boolean handleDiscard(GameState state, Long playerId, List<Card> cards) {
        if (cards == null || cards.size() != 1) return false;
        
        Card discard = cards.get(0);
        List<Card> hand = state.getHands().get(playerId);
        if (!hand.contains(discard)) return false;

        hand.remove(discard);
        Map<String, Object> phomData = state.getGameData();
        
        // Add to shared trash pile
        List<Card> sharedTrash = (List<Card>) phomData.get("trashPile");
        sharedTrash.add(discard);
        
        // Add to player's individual trash pile
        Map<Long, List<Card>> playerTrashes = (Map<Long, List<Card>>) phomData.get("playerTrashPiles");
        playerTrashes.get(playerId).add(discard);
        
        // Update tableCards for frontend (showing last discarded)
        state.setTableCards(List.of(discard));
        
        // Round management
        if (state.getPlayerIds().indexOf(playerId) == state.getPlayerIds().size() - 1) {
            int round = (int) phomData.get("round");
            phomData.put("round", round + 1);
        }

        phomData.put("turnStage", "DRAW");
        state.nextTurn();
        
        checkWin(state);
        return true;
    }

    private boolean handleMeld(GameState state, Long playerId, List<List<Card>> layout) {
        if (layout == null || layout.isEmpty()) return false;
        
        // Simple validation: all cards used in melds must be in player's hand
        List<Card> hand = state.getHands().get(playerId);
        List<Card> allUsed = layout.stream().flatMap(List::stream).toList();
        if (!new HashSet<>(hand).containsAll(allUsed)) return false;

        Map<String, Object> phomData = state.getGameData();
        Map<Long, List<List<Card>>> melds = (Map<Long, List<List<Card>>>) phomData.get("melds");
        melds.put(playerId, layout);
        
        // Remove from hand
        hand.removeAll(allUsed);
        return true;
    }

    private boolean handleAttach(GameState state, Long playerId, List<Card> cards, Long targetPlayerId) {
        if (cards == null || cards.isEmpty() || targetPlayerId == null) return false;
        
        List<Card> hand = state.getHands().get(playerId);
        if (!hand.containsAll(cards)) return false;

        Map<String, Object> phomData = state.getGameData();
        Map<Long, List<List<Card>>> melds = (Map<Long, List<List<Card>>>) phomData.get("melds");
        
        if (!melds.containsKey(targetPlayerId)) return false;
        
        // Add cards to the target player's first meld for simplicity, or we could specify index
        melds.get(targetPlayerId).get(0).addAll(cards);
        hand.removeAll(cards);
        return true;
    }

    @Override
    public void checkWin(GameState state) {
        Map<String, Object> phomData = state.getGameData();
        int round = (int) phomData.get("round");
        Deck deck = (Deck) phomData.get("deck");

        // Ù check (already in hand or recently drawn)
        for (Map.Entry<Long, List<Card>> entry : state.getHands().entrySet()) {
            if (isU(entry.getValue())) {
                state.setWinnerId(entry.getKey());
                state.setGameStarted(false);
                return;
            }
        }

        // End of game check: Deck empty or 4 rounds passed
        if (deck.isEmpty() || round > 4) {
            state.setWinnerId(calculateWinnerByPoints(state));
            state.setGameStarted(false);
        }
    }

    private boolean isU(List<Card> hand) {
        if (hand.isEmpty()) return true;
        // Recursive check for melds (sets or sequences) would go here
        return false; 
    }

    private Long calculateWinnerByPoints(GameState state) {
        Long winnerId = null;
        int minPoints = Integer.MAX_VALUE;

        for (Long pid : state.getPlayerIds()) {
            int score = calculateScore(state.getHands().get(pid));
            if (score < minPoints) {
                minPoints = score;
                winnerId = pid;
            }
        }
        return winnerId;
    }

    private int calculateScore(List<Card> hand) {
        int score = 0;
        for (Card c : hand) {
            int val = c.getRank().getValue();
            // A=14 in Rank enum, but 1 in Phom scoring
            if (val == 14) score += 1;
            // J=11, Q=12, K=13
            else if (val <= 13) score += val;
            // TWO=15 in Rank enum, but 2 in Phom scoring
            else if (val == 15) score += 2;
            else score += val;
        }
        return score;
    }
}

package org.example.backend.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class TienLenLogic implements IGameLogic {

    @Override
    public void sortCards(List<Card> cards) {
        cards.sort(Comparator.comparingInt(Card::getTienLenValue));
    }

    @Override
    public void setupGame(GameState state) {
        Deck deck = new Deck();
        deck.shuffle();
        List<List<Card>> dealtHands = deck.deal(state.getPlayerIds().size(), 13);

        for (int i = 0; i < state.getPlayerIds().size(); i++) {
            Long playerId = state.getPlayerIds().get(i);
            List<Card> hand = dealtHands.get(i);
            sortCards(hand);
            state.getHands().put(playerId, hand);
        }

        state.setGameStarted(true);
        state.setTableCards(new ArrayList<>());
        state.getPassedPlayers().clear();
        state.setWinnerId(null);
        state.setCurrentTurnIndex(0);
        state.setGameType("Tiến Lên");
    }

    @Override
    public boolean handleAction(GameState state, GameAction action) {
        if (!state.isGameStarted()) return false;
        if (!state.getCurrentPlayerId().equals(action.getPlayerId())) return false;

        switch (action.getType()) {
            case PLAY:
                return handlePlay(state, action.getPlayerId(), action.getCards());
            case PASS:
                return handlePass(state, action.getPlayerId());
            default:
                return false;
        }
    }

    private boolean handlePlay(GameState state, Long playerId, List<Card> cardsToPlay) {
        if (isValidMove(cardsToPlay, state.getTableCards())) {
            List<Card> hand = state.getHands().get(playerId);
            hand.removeAll(cardsToPlay);

            state.setTableCards(cardsToPlay);
            state.setLastPlayerId(playerId);
            
            checkWin(state);
            if (state.getWinnerId() == null) {
                state.nextTurn();
            }
            return true;
        }
        return false;
    }

    private boolean handlePass(GameState state, Long playerId) {
        if (state.getTableCards().isEmpty()) return false; // Cannot pass on new round
        state.getPassedPlayers().add(playerId);
        state.nextTurn();
        return true;
    }

    @Override
    public void checkWin(GameState state) {
        for (Map.Entry<Long, List<Card>> entry : state.getHands().entrySet()) {
            if (entry.getValue().isEmpty()) {
                state.setWinnerId(entry.getKey());
                state.setGameStarted(false);
                state.getReadyPlayers().clear();
                return;
            }
        }
    }

    public boolean isValidMove(List<Card> cardsToPlay, List<Card> lastPlayedCards) {
        if (cardsToPlay == null || cardsToPlay.isEmpty()) return false;
        
        CardGroupType type = getGroupType(cardsToPlay);
        if (type == CardGroupType.INVALID) return false;

        // First move in a round
        if (lastPlayedCards == null || lastPlayedCards.isEmpty()) {
            return true;
        }

        CardGroupType lastType = getGroupType(lastPlayedCards);
        
        // Basic comparison (same type, same size)
        if (type == lastType && cardsToPlay.size() == lastPlayedCards.size()) {
            return compareMoves(cardsToPlay, lastPlayedCards) > 0;
        }

        // Special logic for "Chặt" (e.g., Tứ quý chặt Heo, Đôi thông chặt Heo)
        return isSpecialCut(cardsToPlay, lastPlayedCards, type, lastType);
    }

    public int compareMoves(List<Card> newCards, List<Card> oldCards) {
        // In Tien Len, the strength of a group is determined by its highest card
        Card highestNew = getHighestCard(newCards);
        Card highestOld = getHighestCard(oldCards);
        return Integer.compare(highestNew.getTienLenValue(), highestOld.getTienLenValue());
    }

    private boolean isSpecialCut(List<Card> newMove, List<Card> oldMove, CardGroupType newType, CardGroupType oldType) {
        // Tứ quý chặt Heo đơn
        if (newType == CardGroupType.FOUR_OF_A_KIND && oldType == CardGroupType.SINGLE && oldMove.get(0).getRank() == Rank.TWO) {
            return true;
        }
        // Tứ quý chặt Đôi Heo
        if (newType == CardGroupType.FOUR_OF_A_KIND && oldType == CardGroupType.PAIR && oldMove.get(0).getRank() == Rank.TWO) {
            return true;
        }
        // 3 đôi thông chặt Heo đơn
        if (newType == CardGroupType.THREE_PAIR_SEQUENCE && oldType == CardGroupType.SINGLE && oldMove.get(0).getRank() == Rank.TWO) {
            return true;
        }
        // Tứ quý chặt Tứ quý nhỏ hơn (handled by basic comparison if types match, but explicitly here for clarity)
        return false;
    }

    private Card getHighestCard(List<Card> cards) {
        List<Card> sorted = new ArrayList<>(cards);
        sortCards(sorted);
        return sorted.get(sorted.size() - 1);
    }

    public CardGroupType getGroupType(List<Card> cards) {
        int size = cards.size();
        if (size == 0) return CardGroupType.INVALID;
        
        List<Card> sorted = new ArrayList<>(cards);
        sortCards(sorted);

        if (size == 1) return CardGroupType.SINGLE;
        if (size == 2 && isSameRank(cards)) return CardGroupType.PAIR;
        if (size == 3 && isSameRank(cards)) return CardGroupType.TRIPLE;
        if (size == 4 && isSameRank(cards)) return CardGroupType.FOUR_OF_A_KIND;

        if (size >= 3 && isSequence(sorted)) return CardGroupType.STRAIGHT;
        if (size >= 6 && size % 2 == 0 && isPairSequence(sorted)) {
            if (size == 6) return CardGroupType.THREE_PAIR_SEQUENCE;
            if (size == 8) return CardGroupType.FOUR_PAIR_SEQUENCE;
        }

        return CardGroupType.INVALID;
    }

    private boolean isSameRank(List<Card> cards) {
        Rank first = cards.get(0).getRank();
        return cards.stream().allMatch(c -> c.getRank() == first);
    }

    private boolean isSequence(List<Card> sortedCards) {
        // 2 cannot be in a straight in Tien Len (usually)
        if (sortedCards.stream().anyMatch(c -> c.getRank() == Rank.TWO)) return false;
        
        for (int i = 0; i < sortedCards.size() - 1; i++) {
            if (sortedCards.get(i + 1).getRank().getValue() != sortedCards.get(i).getRank().getValue() + 1) {
                return false;
            }
        }
        return true;
    }

    private boolean isPairSequence(List<Card> sortedCards) {
        // Pairs must be 3, 3, 4, 4, 5, 5... (no 2s)
        if (sortedCards.stream().anyMatch(c -> c.getRank() == Rank.TWO)) return false;
        
        for (int i = 0; i < sortedCards.size(); i += 2) {
            if (sortedCards.get(i).getRank() != sortedCards.get(i + 1).getRank()) return false;
            if (i + 2 < sortedCards.size()) {
                if (sortedCards.get(i + 2).getRank().getValue() != sortedCards.get(i).getRank().getValue() + 1) {
                    return false;
                }
            }
        }
        return true;
    }

    public enum CardGroupType {
        INVALID,
        SINGLE,
        PAIR,
        TRIPLE,
        FOUR_OF_A_KIND,
        STRAIGHT,
        THREE_PAIR_SEQUENCE, // 3 đôi thông
        FOUR_PAIR_SEQUENCE   // 4 đôi thông
    }
}

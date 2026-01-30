package org.example.backend.game;

import lombok.Data;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Data
public class GameState {
    private String roomId;
    private List<Long> playerIds = new ArrayList<>();
    private Map<Long, String> displayNames = new ConcurrentHashMap<>();
    private Map<Long, String> rankTiers = new ConcurrentHashMap<>();
    private Map<Long, Integer> rankPoints = new ConcurrentHashMap<>();
    private Map<Long, List<Card>> hands = new ConcurrentHashMap<>();
    private List<Card> tableCards = new ArrayList<>();
    private Long lastPlayerId;
    private int currentTurnIndex = 0;
    private boolean isGameStarted = false;
    private Set<Long> passedPlayers = new HashSet<>();
    private Set<Long> readyPlayers = new HashSet<>();
    private Long hostId;
    private Long turnStartTime; // System.currentTimeMillis()
    private Long winnerId;

    public GameState(String roomId) {
        this.roomId = roomId;
    }

    public void addPlayer(Long playerId, String displayName, String rankTier, Integer rankPoints) {
        if (!playerIds.contains(playerId)) {
            playerIds.add(playerId);
        }
        
        // Always update details
        displayNames.put(playerId, displayName);
        rankTiers.put(playerId, rankTier);
        this.rankPoints.put(playerId, rankPoints);

        if (hostId == null) {
            hostId = playerId;
        }
    }

    public void removePlayer(Long playerId) {
        playerIds.remove(playerId);
        displayNames.remove(playerId);
        rankTiers.remove(playerId);
        rankPoints.remove(playerId);
        hands.remove(playerId);
        readyPlayers.remove(playerId);
        if (playerId.equals(hostId)) {
            hostId = playerIds.isEmpty() ? null : playerIds.get(0);
        }
    }

    public Long getCurrentPlayerId() {
        if (playerIds.isEmpty()) return null;
        return playerIds.get(currentTurnIndex);
    }

    public void nextTurn() {
        if (playerIds.isEmpty()) return;
        
        do {
            currentTurnIndex = (currentTurnIndex + 1) % playerIds.size();
        } while (passedPlayers.contains(playerIds.get(currentTurnIndex)) && passedPlayers.size() < playerIds.size() - 1);
        
        // If everyone else passed, reset table and passed status
        if (passedPlayers.size() >= playerIds.size() - 1 && !passedPlayers.contains(getCurrentPlayerId())) {
            tableCards.clear();
            passedPlayers.clear();
            lastPlayerId = null;
        }
    }
}

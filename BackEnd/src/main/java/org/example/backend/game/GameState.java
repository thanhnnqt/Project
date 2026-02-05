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
    private Map<Long, String> avatars = new ConcurrentHashMap<>();
    private Map<Long, String> equippedFrames = new ConcurrentHashMap<>(); // Stores CSS class for avatar frame
    private Map<Long, String> playerCardFrames = new ConcurrentHashMap<>(); // Stores CSS class for large card frame
    private Map<Long, String> cardSkins = new ConcurrentHashMap<>();       // Stores ID for card skin
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
    private int maxPlayers = 4;
    private List<org.example.backend.entity.ChatMessage> chatHistory = new ArrayList<>();

    public GameState(String roomId) {
        this.roomId = roomId;
    }

    public void addPlayer(Long playerId, String displayName, String rankTier, Integer rankPoints, String frameEffect, String cardEffect, String cardSkin, String avatar) {
        if (!playerIds.contains(playerId)) {
            playerIds.add(playerId);
        }
        
        // Always update details
        displayNames.put(playerId, displayName);
        rankTiers.put(playerId, rankTier);
        this.rankPoints.put(playerId, rankPoints);
        if (avatar != null) {
            avatars.put(playerId, avatar);
        } else {
            avatars.remove(playerId);
        }
        if (frameEffect != null) {
            equippedFrames.put(playerId, frameEffect);
        } else {
            equippedFrames.remove(playerId);
        }
        if (cardEffect != null) {
            playerCardFrames.put(playerId, cardEffect);
        } else {
            playerCardFrames.remove(playerId);
        }
        if (cardSkin != null) {
            cardSkins.put(playerId, cardSkin);
        } else {
            cardSkins.remove(playerId);
        }

        if (hostId == null) {
            hostId = playerId;
        }
    }

    public void removePlayer(Long playerId) {
        playerIds.remove(playerId);
        displayNames.remove(playerId);
        rankTiers.remove(playerId);
        rankPoints.remove(playerId);
        avatars.remove(playerId);
        equippedFrames.remove(playerId);
        playerCardFrames.remove(playerId);
        cardSkins.remove(playerId);
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

package org.example.backend.game;

import lombok.RequiredArgsConstructor;
import org.example.backend.dto.GameMessage;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.TaskScheduler;
import java.time.Instant;
import java.util.concurrent.ScheduledFuture;

import org.example.backend.service.IPlayerService;
import java.math.BigDecimal;

import org.example.backend.service.impl.RankService;

@Service
@RequiredArgsConstructor
public class GameManager {
    private final SimpMessagingTemplate messagingTemplate;
    private final TaskScheduler taskScheduler;
    private final IPlayerService playerService;
    private final RankService rankService;
    private final org.example.backend.service.IRoomService roomService;
    
    private final Map<String, GameState> games = new ConcurrentHashMap<>();
    private final Map<String, ScheduledFuture<?>> turnTimers = new ConcurrentHashMap<>();
    
    // Logic Registry - Keys must match or contain database names
    private final Map<String, IGameLogic> logics = new ConcurrentHashMap<>() {{
        put("Tiến Lên Miền Nam", new TienLenLogic());
        put("Phỏm (Tá Lả)", new PhomLogic());
        put("Mậu Binh", new BinhLogic());
        put("Poker (Texas Hold'em)", new PokerLogic());
    }};

    private IGameLogic getLogic(String gameType) {
        if (gameType == null) return logics.get("Tiến Lên Miền Nam");
        for (Map.Entry<String, IGameLogic> entry : logics.entrySet()) {
            if (gameType.toLowerCase().contains(entry.getKey().toLowerCase()) || 
                entry.getKey().toLowerCase().contains(gameType.toLowerCase())) {
                return entry.getValue();
            }
        }
        return logics.get("Tiến Lên Miền Nam");
    }

    private void updateBalances(GameState game, Long winnerId) {
        BigDecimal minBet = roomService.findById(Long.parseLong(game.getRoomId()))
                .map(room -> room.getMinBet())
                .orElse(new BigDecimal("100.00"));

        int playerCount = game.getPlayerIds().size();
        BigDecimal totalWinnings = minBet.multiply(new BigDecimal(playerCount - 1));

        for (Long playerId : game.getPlayerIds()) {
            playerService.findById(playerId).ifPresent(player -> {
                if (playerId.equals(winnerId)) {
                    player.setBalance(player.getBalance().add(totalWinnings));
                    BigDecimal promotionReward = rankService.processWin(player);
                    if (promotionReward.compareTo(BigDecimal.ZERO) > 0) {
                        player.setBalance(player.getBalance().add(promotionReward));
                    }
                } else {
                    player.setBalance(player.getBalance().subtract(minBet));
                    rankService.processLoss(player);
                }
                playerService.save(player);
                game.getRankTiers().put(playerId, player.getRankTier());
                game.getRankPoints().put(playerId, player.getRankPoints());
            });
        }
    }

    private void startTurnTimer(String roomId) {
        resetTurnTimer(roomId);
        GameState game = games.get(roomId);
        if (game == null || !game.isGameStarted()) return;

        game.setTurnStartTime(System.currentTimeMillis());
        
        ScheduledFuture<?> timer = taskScheduler.schedule(() -> {
            autoPlayAction(roomId);
        }, Instant.now().plusSeconds(15));
        
        turnTimers.put(roomId, timer);
    }

    private void resetTurnTimer(String roomId) {
        ScheduledFuture<?> timer = turnTimers.remove(roomId);
        if (timer != null) {
            timer.cancel(false);
        }
    }

    private void autoPlayAction(String roomId) {
        GameState game = games.get(roomId);
        if (game == null || !game.isGameStarted()) return;

        Long currentPlayerId = game.getCurrentPlayerId();
        
        // Default Auto-play logic (mainly for Tien Len)
        if ("Tiến Lên".equals(game.getGameType())) {
            List<Card> hand = game.getHands().get(currentPlayerId);
            if (hand == null || hand.isEmpty()) return;

            if (game.getTableCards().isEmpty()) {
                handleAction(roomId, GameAction.builder()
                        .type(GameAction.ActionType.PLAY)
                        .playerId(currentPlayerId)
                        .cards(List.of(hand.get(0)))
                        .build());
            } else {
                handleAction(roomId, GameAction.builder()
                        .type(GameAction.ActionType.PASS)
                        .playerId(currentPlayerId)
                        .build());
            }
        }
        
        broadcastUpdate(roomId, game);
    }

    public GameState getOrCreateGame(String roomId) {
        return games.computeIfAbsent(roomId, id -> {
            GameState state = new GameState(id);
            try {
                roomService.findById(Long.parseLong(id)).ifPresent(room -> {
                    if (room.getGameType() != null) {
                        state.setGameType(room.getGameType().getName());
                    }
                });
            } catch (Exception e) {
                // Ignore parsing errors for non-numeric room IDs if any
            }
            return state;
        });
    }

    public void startGame(String roomId, Long requesterId) {
        GameState game = games.get(roomId);
        if (game == null) return;
        if (!requesterId.equals(game.getHostId())) return;

        boolean allReady = game.getPlayerIds().stream()
                .filter(id -> !id.equals(game.getHostId()))
                .allMatch(id -> game.getReadyPlayers().contains(id));

        if (!allReady || game.getPlayerIds().size() < 2) return;

        getLogic(game.getGameType()).setupGame(game);
        startTurnTimer(roomId);
    }

    public void toggleReady(String roomId, Long playerId) {
        GameState game = games.get(roomId);
        if (game == null || game.isGameStarted()) return;
        
        if (game.getReadyPlayers().contains(playerId)) {
            game.getReadyPlayers().remove(playerId);
        } else {
            game.getReadyPlayers().add(playerId);
        }
    }

    public boolean kickPlayer(String roomId, Long hostId, Long targetId) {
        GameState game = games.get(roomId);
        if (game == null || game.isGameStarted()) return false;
        if (hostId.equals(game.getHostId()) && !hostId.equals(targetId)) {
            game.removePlayer(targetId);
            return true;
        }
        return false;
    }

    public boolean handleAction(String roomId, GameAction action) {
        GameState game = games.get(roomId);
        if (game == null || !game.isGameStarted()) return false;

        IGameLogic logic = getLogic(game.getGameType());
        boolean success = logic.handleAction(game, action);

        if (success) {
            resetTurnTimer(roomId);
            if (game.getWinnerId() != null) {
                game.setGameStarted(false);
                updateBalances(game, game.getWinnerId());
            } else {
                startTurnTimer(roomId);
            }
            return true;
        }
        return false;
    }

    // Deprecated but kept for backward compatibility during refactor if needed
    public boolean handlePlay(String roomId, Long playerId, List<Card> cards) {
        return handleAction(roomId, GameAction.builder()
                .type(GameAction.ActionType.PLAY)
                .playerId(playerId)
                .cards(cards)
                .build());
    }

    public void handlePass(String roomId, Long playerId) {
        handleAction(roomId, GameAction.builder()
                .type(GameAction.ActionType.PASS)
                .playerId(playerId)
                .build());
    }

    public void resetRoom(String roomId, Long requesterId) {
        GameState game = games.get(roomId);
        if (game == null || game.isGameStarted()) return;
        if (requesterId.equals(game.getHostId())) {
            game.setWinnerId(null);
            game.setTableCards(new ArrayList<>());
        }
    }

    public void deleteGame(String roomId) {
        games.remove(roomId);
        resetTurnTimer(roomId);
    }

    private void broadcastUpdate(String roomId, GameState game) {
        messagingTemplate.convertAndSend("/topic/game/" + roomId, 
            GameMessage.builder().type(GameMessage.MessageType.UPDATE).payload(game).build());
    }
}

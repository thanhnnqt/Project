package org.example.backend.game;

import lombok.RequiredArgsConstructor;
import org.example.backend.dto.GameMessage;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.TaskScheduler;
import java.time.Instant;
import java.util.concurrent.ScheduledFuture;

import org.example.backend.service.IPlayerService;
import java.math.BigDecimal;

import org.example.backend.service.RankService;

@Service
@RequiredArgsConstructor
public class GameManager {
    private final SimpMessagingTemplate messagingTemplate;
    private final TaskScheduler taskScheduler;
    private final IPlayerService playerService;
    private final RankService rankService;
    private final Map<String, GameState> games = new ConcurrentHashMap<>();
    private final Map<String, ScheduledFuture<?>> turnTimers = new ConcurrentHashMap<>();
    private final TienLenLogic tienLenLogic = new TienLenLogic();

    private void updateBalances(GameState game, Long winnerId) {
        for (Long playerId : game.getPlayerIds()) {
            playerService.findById(playerId).ifPresent(player -> {
                if (playerId.equals(winnerId)) {
                    // Winner gets +100
                    player.setBalance(player.getBalance().add(new BigDecimal("100.00")));
                    // Update Rank
                    rankService.processWin(player);
                } else {
                    // Losers get -20
                    player.setBalance(player.getBalance().subtract(new BigDecimal("20.00")));
                    // Loss Rank
                    rankService.processLoss(player);
                }
                playerService.save(player);
                
                // Sync GameState
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
            autoPlaySmallestCard(roomId);
        }, Instant.now().plusSeconds(15));
        
        turnTimers.put(roomId, timer);
    }

    private void resetTurnTimer(String roomId) {
        ScheduledFuture<?> timer = turnTimers.remove(roomId);
        if (timer != null) {
            timer.cancel(false);
        }
    }

    private void autoPlaySmallestCard(String roomId) {
        GameState game = games.get(roomId);
        if (game == null || !game.isGameStarted()) return;

        Long currentPlayerId = game.getCurrentPlayerId();
        List<Card> hand = game.getHands().get(currentPlayerId);
        if (hand == null || hand.isEmpty()) return;

        List<Card> cardsToPlay = new java.util.ArrayList<>();
        if (game.getTableCards().isEmpty()) {
            // Leader: Play smallest card
            cardsToPlay.add(hand.get(0));
            handlePlay(roomId, currentPlayerId, cardsToPlay);
        } else {
            // Following: Just pass instead of playing
            handlePass(roomId, currentPlayerId);
        }
        
        // Broadcast the update after auto-play
        messagingTemplate.convertAndSend("/topic/game/" + roomId, 
            GameMessage.builder().type(GameMessage.MessageType.UPDATE).payload(game).build());
    }

    public GameState getOrCreateGame(String roomId) {
        return games.computeIfAbsent(roomId, GameState::new);
    }

    public void startGame(String roomId, Long requesterId) {
        GameState game = games.get(roomId);
        if (game == null) return;
        
        // 1. Only host can start
        if (!requesterId.equals(game.getHostId())) return;
        
        // 2. All other players must be ready
        boolean allReady = true;
        for (Long playerId : game.getPlayerIds()) {
            if (!playerId.equals(game.getHostId()) && !game.getReadyPlayers().contains(playerId)) {
                allReady = false;
                break;
            }
        }
        
        if (!allReady || game.getPlayerIds().size() < 2) return;

        Deck deck = new Deck();
        deck.shuffle();
        List<List<Card>> dealtHands = deck.deal(game.getPlayerIds().size(), 13);

        for (int i = 0; i < game.getPlayerIds().size(); i++) {
            Long playerId = game.getPlayerIds().get(i);
            List<Card> hand = dealtHands.get(i);
            tienLenLogic.sortCards(hand);
            game.getHands().put(playerId, hand);
        }

        game.setGameStarted(true);
        game.setTableCards(new java.util.ArrayList<>());
        game.getPassedPlayers().clear();
        game.setWinnerId(null);
        game.setCurrentTurnIndex(0);
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
        
        // Only host can kick and cannot kick themselves
        if (hostId.equals(game.getHostId()) && !hostId.equals(targetId)) {
            game.removePlayer(targetId);
            return true;
        }
        return false;
    }

    public boolean handlePlay(String roomId, Long playerId, List<Card> cardsToPlay) {
        GameState game = games.get(roomId);
        if (game == null || !game.isGameStarted()) return false;
        if (!game.getCurrentPlayerId().equals(playerId)) return false;

        if (tienLenLogic.isValidMove(cardsToPlay, game.getTableCards())) {
            // Remove cards from hand
            List<Card> hand = game.getHands().get(playerId);
            hand.removeAll(cardsToPlay);

            // Update table
            game.setTableCards(cardsToPlay);
            game.setLastPlayerId(playerId);
            resetTurnTimer(roomId);

            // Check if win
            if (hand.isEmpty()) {
                game.setWinnerId(playerId);
                game.setGameStarted(false);
                game.getReadyPlayers().clear(); // Reset sẵn sàng cho ván mới
                updateBalances(game, playerId); // Lưu điểm/xu vào Database
                return true;
            }

            game.nextTurn();
            startTurnTimer(roomId);
            return true;
        }
        return false;
    }

    public void resetRoom(String roomId, Long requesterId) {
        GameState game = games.get(roomId);
        if (game == null || game.isGameStarted()) return;
        
        // Only host can reset
        if (requesterId.equals(game.getHostId())) {
            game.setWinnerId(null);
            game.setTableCards(new java.util.ArrayList<>());
        }
    }

    public void deleteGame(String roomId) {
        games.remove(roomId);
        resetTurnTimer(roomId);
    }

    public void handlePass(String roomId, Long playerId) {
        GameState game = games.get(roomId);
        if (game == null || !game.isGameStarted()) return;
        if (!game.getCurrentPlayerId().equals(playerId)) return;
        
        // Rule: Cannot pass if table is empty (starting a new round)
        if (game.getTableCards().isEmpty()) {
            messagingTemplate.convertAndSend("/topic/game/" + roomId, 
                GameMessage.builder().type(GameMessage.MessageType.ERROR).content("Không thể bỏ lượt khi bắt đầu vòng mới!").build());
            return;
        }

        resetTurnTimer(roomId);
        game.getPassedPlayers().add(playerId);
        game.nextTurn();
        startTurnTimer(roomId);
    }
}

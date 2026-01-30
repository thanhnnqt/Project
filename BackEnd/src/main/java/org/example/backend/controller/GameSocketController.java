package org.example.backend.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.dto.GameMessage;
import org.example.backend.game.Card;
import org.example.backend.game.GameManager;
import org.example.backend.entity.Player;
import org.example.backend.game.GameState;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.List;

import org.example.backend.service.IPlayerService;

@Controller
@RequiredArgsConstructor
public class GameSocketController {
    private final SimpMessagingTemplate messagingTemplate;
    private final GameManager gameManager;
    private final IPlayerService playerService;
    private final org.example.backend.service.IRoomService roomService;

    @MessageMapping("/game.join/{roomId}")
    public void joinGame(@DestinationVariable String roomId, @Payload Long playerId, org.springframework.messaging.simp.SimpMessageHeaderAccessor headerAccessor) {
        GameState game = gameManager.getOrCreateGame(roomId);
        Player player = playerService.findById(playerId).orElse(null);
        
        String name = (player != null) ? player.getDisplayName() : "Người chơi " + playerId;
        String rank = (player != null) ? player.getRankTier() : "Sắt IV";
        Integer points = (player != null) ? player.getRankPoints() : 0;
        
        game.addPlayer(playerId, name, rank, points);

        // Store session attributes for disconnect handling
        headerAccessor.getSessionAttributes().put("roomId", roomId);
        headerAccessor.getSessionAttributes().put("playerId", playerId);
        
        broadcastUpdate(roomId, game);
    }

    @MessageMapping("/game.leave/{roomId}")
    public void leaveGame(@DestinationVariable String roomId, @Payload Long playerId) {
        GameState game = gameManager.getOrCreateGame(roomId);
        game.removePlayer(playerId);
        
        if (game.getPlayerIds().isEmpty()) {
            // Xóa khỏi Database
            try {
                roomService.deleteById(Long.parseLong(roomId));
            } catch (Exception e) {
                // Log error if needed
            }
            // Xóa khỏi bộ nhớ
            gameManager.deleteGame(roomId);
        } else {
            broadcastUpdate(roomId, game);
        }
    }

    @MessageMapping("/game.start/{roomId}")
    public void startGame(@DestinationVariable String roomId, @Payload Long playerId) {
        gameManager.startGame(roomId, playerId);
        GameState game = gameManager.getOrCreateGame(roomId);
        
        broadcastUpdate(roomId, game);
    }

    @MessageMapping("/game.ready/{roomId}")
    public void toggleReady(@DestinationVariable String roomId, @Payload Long playerId) {
        gameManager.toggleReady(roomId, playerId);
        GameState game = gameManager.getOrCreateGame(roomId);
        
        broadcastUpdate(roomId, game);
    }

    @MessageMapping("/game.kick/{roomId}")
    public void kickPlayer(@DestinationVariable String roomId, @Payload KickRequest request) {
        boolean success = gameManager.kickPlayer(roomId, request.getHostId(), request.getTargetId());
        if (success) {
            // Notify the specific player they were kicked
            messagingTemplate.convertAndSend("/topic/game/" + roomId, 
                GameMessage.builder()
                    .type(GameMessage.MessageType.KICKED)
                    .payload(request.getTargetId())
                    .content("Bạn đã bị chủ phòng mời ra khỏi phòng.")
                    .build());
            
            // Broadcast room update to others
            broadcastUpdate(roomId, gameManager.getOrCreateGame(roomId));
        }
    }

    @MessageMapping("/game.reset/{roomId}")
    public void resetRoom(@DestinationVariable String roomId, @Payload Long playerId) {
        gameManager.resetRoom(roomId, playerId);
        GameState game = gameManager.getOrCreateGame(roomId);
        
        broadcastUpdate(roomId, game);
    }

    @MessageMapping("/game.play/{roomId}")
    public void playMove(@DestinationVariable String roomId, @Payload PlayerMove move) {
        boolean success = gameManager.handlePlay(roomId, move.getPlayerId(), move.getCards());
        GameState game = gameManager.getOrCreateGame(roomId);

        if (success) {
            if (game.getWinnerId() != null) {
                messagingTemplate.convertAndSend("/topic/game/" + roomId, 
                    GameMessage.builder().type(GameMessage.MessageType.WINNER).payload(game).build());
            } else {
                broadcastUpdate(roomId, game);
            }
        } else {
            messagingTemplate.convertAndSend("/topic/game/" + roomId, 
                GameMessage.builder().type(GameMessage.MessageType.ERROR).content("Nước đi không hợp lệ!").build());
        }
    }

    @MessageMapping("/game.pass/{roomId}")
    public void passTurn(@DestinationVariable String roomId, @Payload Long playerId) {
        gameManager.handlePass(roomId, playerId);
        GameState game = gameManager.getOrCreateGame(roomId);
        
        broadcastUpdate(roomId, game);
    }

    @MessageMapping("/game.chat/{roomId}")
    public void handleChat(@DestinationVariable String roomId, @Payload ChatRequest request) {
        messagingTemplate.convertAndSend("/topic/game/" + roomId, 
            GameMessage.builder()
                .type(GameMessage.MessageType.CHAT)
                .payload(request.getPlayerId())
                .content(request.getMessage())
                .build());
    }

    @MessageMapping("/game.emoji/{roomId}")
    public void handleEmoji(@DestinationVariable String roomId, @Payload EmojiRequest request) {
        messagingTemplate.convertAndSend("/topic/game/" + roomId, 
            GameMessage.builder()
                .type(GameMessage.MessageType.EMOJI)
                .payload(request.getPlayerId())
                .content(request.getEmoji())
                .build());
    }

    private void broadcastUpdate(String roomId, GameState game) {
        messagingTemplate.convertAndSend("/topic/game/" + roomId, 
            GameMessage.builder().type(GameMessage.MessageType.UPDATE).payload(game).build());
    }

    // Inner class for move payload
    public static class PlayerMove {
        private Long playerId;
        private List<Card> cards;
        public Long getPlayerId() { return playerId; }
        public List<Card> getCards() { return cards; }
        public void setPlayerId(Long playerId) { this.playerId = playerId; }
        public void setCards(List<Card> cards) { this.cards = cards; }
    }

    public static class KickRequest {
        private Long hostId;
        private Long targetId;
        public Long getHostId() { return hostId; }
        public Long getTargetId() { return targetId; }
        public void setHostId(Long hostId) { this.hostId = hostId; }
        public void setTargetId(Long targetId) { this.targetId = targetId; }
    }

    @lombok.Data
    public static class ChatRequest {
        private Long playerId;
        private String message;
    }

    @lombok.Data
    public static class EmojiRequest {
        private Long playerId;
        private String emoji;
    }
}


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
import java.util.Optional;
import org.example.backend.service.IPlayerService;

@Controller
@RequiredArgsConstructor
public class GameSocketController {
    private final SimpMessagingTemplate messagingTemplate;
    private final GameManager gameManager;
    private final IPlayerService playerService;
    private final org.example.backend.service.IRoomService roomService;
    private final org.example.backend.service.FirebaseChatService firebaseChatService;
    private final org.example.backend.repository.IPlayerInventoryRepository inventoryRepository;

    @MessageMapping("/game.join/{roomId}")
    public void joinGame(@DestinationVariable String roomId, @Payload Long playerId, org.springframework.messaging.simp.SimpMessageHeaderAccessor headerAccessor) {
        GameState game = gameManager.getOrCreateGame(roomId);
        Player player = playerService.findById(playerId).orElse(null);
        
        // Initialize maxPlayers if not already synced with DB
        roomService.findById(Long.parseLong(roomId)).ifPresent(room -> {
            game.setMaxPlayers(room.getMaxPlayers() != null ? room.getMaxPlayers() : 4);
        });

        // Enforce maxPlayers limit
        if (!game.getPlayerIds().contains(playerId) && game.getPlayerIds().size() >= game.getMaxPlayers()) {
            messagingTemplate.convertAndSendToUser(playerId.toString(), "/topic/errors", "Phòng đã đầy!");
            return;
        }

        String name = (player != null) ? player.getDisplayName() : "Người chơi " + playerId;
        String rank = (player != null) ? player.getRankTier() : "Sắt IV";
        Integer points = (player != null) ? player.getRankPoints() : 0;
        String avatar = (player != null) ? player.getAvatar() : null;
        
        // Get equipped avatar frame and player card frame
        String frameEffect = inventoryRepository.findEquippedItemByType(playerId, "AVATAR_FRAME")
                .map(pi -> pi.getShopItem().getImageUrl())
                .orElse(null);
        String playerCardFrame = inventoryRepository.findEquippedItemByType(playerId, "PLAYER_CARD_FRAME")
                .map(pi -> pi.getShopItem().getImageUrl())
                .orElse(null);
        String cardSkin = inventoryRepository.findEquippedItemByType(playerId, "CARD_SKIN")
                .map(pi -> pi.getShopItem().getImageUrl())
                .orElse(null);

        game.addPlayer(playerId, name, rank, points, frameEffect, playerCardFrame, cardSkin, avatar);

        // Store session attributes for disconnect handling
        headerAccessor.getSessionAttributes().put("roomId", roomId);
        headerAccessor.getSessionAttributes().put("playerId", playerId);

        // Initialize empty history for instant join
        game.setChatHistory(new java.util.ArrayList<>());
        broadcastUpdate(roomId, game);

        // Load chat history from Firebase asynchronously
        java.util.concurrent.CompletableFuture.runAsync(() -> {
            try {
                List<org.example.backend.entity.ChatMessage> history = firebaseChatService.getChatHistory(roomId);
                if (!history.isEmpty()) {
                    game.setChatHistory(history);
                    broadcastUpdate(roomId, game);
                }
            } catch (Exception e) {
                System.err.println("Firebase history load failed.");
            }
        });
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
        Player player = playerService.findById(request.getPlayerId()).orElse(null);
        String playerName = (player != null) ? player.getDisplayName() : "Người chơi " + request.getPlayerId();

        org.example.backend.entity.ChatMessage chatMsg = org.example.backend.entity.ChatMessage.builder()
                .roomId(roomId)
                .playerId(request.getPlayerId())
                .playerName(playerName)
                .message(request.getMessage())
                .timestamp(System.currentTimeMillis())
                .build();
        GameState game = gameManager.getOrCreateGame(roomId);
        
        // Add to RAM history (Circular buffer 50 messages)
        List<org.example.backend.entity.ChatMessage> history = game.getChatHistory();
        history.add(chatMsg);
        if (history.size() > 50) {
            history.remove(0);
        }

        // Save to Firebase asynchronously
        java.util.concurrent.CompletableFuture.runAsync(() -> {
            firebaseChatService.saveMessage(chatMsg);
        });

        messagingTemplate.convertAndSend("/topic/game/" + roomId, 
            GameMessage.builder()
                .type(GameMessage.MessageType.CHAT)
                .payload(chatMsg)
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


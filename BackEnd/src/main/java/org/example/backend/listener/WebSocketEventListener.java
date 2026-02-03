package org.example.backend.listener;

import lombok.RequiredArgsConstructor;
import org.example.backend.dto.GameMessage;
import org.example.backend.game.GameManager;
import org.example.backend.game.GameState;
import org.example.backend.service.IRoomService;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final GameManager gameManager;
    private final IRoomService roomService;
    private final SimpMessagingTemplate messagingTemplate;

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());

        String roomId = (String) headerAccessor.getSessionAttributes().get("roomId");
        Long playerId = (Long) headerAccessor.getSessionAttributes().get("playerId");

        if (roomId != null && playerId != null) {
            GameState game = gameManager.getOrCreateGame(roomId);
            game.removePlayer(playerId);

            if (game.getPlayerIds().isEmpty()) {
                try {
                    // Check if room exists before deleting to avoid exception
                    Long roomIdLong = Long.parseLong(roomId);
                    if (roomService.findById(roomIdLong).isPresent()) {
                        roomService.deleteById(roomIdLong);
                    } else {
                        System.out.println("Room " + roomId + " already deleted or not found");
                    }
                } catch (Exception e) {
                    // Handle potential errors
                    System.out.println("Room " + roomId + " deletion skipped: " + e.getMessage());
                }
                gameManager.deleteGame(roomId);
            } else {
                messagingTemplate.convertAndSend("/topic/game/" + roomId,
                        GameMessage.builder().type(GameMessage.MessageType.UPDATE).payload(game).build());
            }
        }
    }
}

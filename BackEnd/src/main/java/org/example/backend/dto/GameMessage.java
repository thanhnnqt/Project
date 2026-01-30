package org.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GameMessage {
    private MessageType type;
    private String content;
    private String sender;
    private Object payload;

    public enum MessageType {
        JOIN,
        LEAVE,
        START,
        PLAY,
        PASS,
        UPDATE,
        ERROR,
        WINNER,
        READY,
        KICKED,
        RESET,
        CHAT,
        EMOJI
    }
}

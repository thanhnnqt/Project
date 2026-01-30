package org.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.backend.entity.Room;
import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RoomResponse {
    private Long id;
    private String name;
    private Long gameTypeId;
    private String gameTypeName;
    private BigDecimal minBet;
    private int currentPlayerCount;
    private int maxPlayers;
    private String status;

    public static RoomResponse fromEntity(Room room, int currentPlayerCount) {
        return RoomResponse.builder()
                .id(room.getId())
                .name(room.getName())
                .gameTypeId(room.getGameType().getId())
                .gameTypeName(room.getGameType().getName())
                .minBet(room.getMinBet())
                .currentPlayerCount(currentPlayerCount)
                .maxPlayers(room.getGameType().getMaxPlayers())
                .status(room.getStatus())
                .build();
    }
}

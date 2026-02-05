package org.example.backend.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class CreateRoomRequest {
    private String name;
    private Long gameTypeId;
    private BigDecimal minBet;
    private Long hostId;
    private String password;
    private Integer maxPlayers;
}

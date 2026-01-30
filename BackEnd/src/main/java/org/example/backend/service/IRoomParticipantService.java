package org.example.backend.service;

import org.example.backend.entity.RoomParticipant;
import java.util.List;
import java.util.Optional;

public interface IRoomParticipantService {
    List<RoomParticipant> findByRoomId(Long roomId);
    Optional<RoomParticipant> findByRoomIdAndPlayerId(Long roomId, Long playerId);
    RoomParticipant save(RoomParticipant participant);
    void removePlayerFromRoom(Long roomId, Long playerId);
}

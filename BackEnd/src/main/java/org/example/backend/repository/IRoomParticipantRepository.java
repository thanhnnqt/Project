package org.example.backend.repository;

import org.example.backend.entity.RoomParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface IRoomParticipantRepository extends JpaRepository<RoomParticipant, Long> {
    List<RoomParticipant> findByRoomId(Long roomId);
    Optional<RoomParticipant> findByRoomIdAndPlayerId(Long roomId, Long playerId);
    void deleteByRoomIdAndPlayerId(Long roomId, Long playerId);
}

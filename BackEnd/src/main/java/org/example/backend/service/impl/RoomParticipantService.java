package org.example.backend.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.backend.entity.RoomParticipant;
import org.example.backend.repository.IRoomParticipantRepository;
import org.example.backend.service.IRoomParticipantService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RoomParticipantService implements IRoomParticipantService {
    private final IRoomParticipantRepository participantRepository;

    @Override
    public List<RoomParticipant> findByRoomId(Long roomId) {
        return participantRepository.findByRoomId(roomId);
    }

    @Override
    public Optional<RoomParticipant> findByRoomIdAndPlayerId(Long roomId, Long playerId) {
        return participantRepository.findByRoomIdAndPlayerId(roomId, playerId);
    }

    @Override
    public RoomParticipant save(RoomParticipant participant) {
        return participantRepository.save(participant);
    }

    @Override
    @Transactional
    public void removePlayerFromRoom(Long roomId, Long playerId) {
        participantRepository.deleteByRoomIdAndPlayerId(roomId, playerId);
    }
}

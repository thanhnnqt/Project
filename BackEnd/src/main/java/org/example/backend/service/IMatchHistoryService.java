package org.example.backend.service;

import org.example.backend.entity.MatchHistory;
import java.util.List;
import java.util.Optional;

public interface IMatchHistoryService {
    List<MatchHistory> findAll();
    Optional<MatchHistory> findById(Long id);
    MatchHistory save(MatchHistory matchHistory);
    List<MatchHistory> findByRoomId(Long roomId);
}

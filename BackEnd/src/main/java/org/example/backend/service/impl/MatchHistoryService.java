package org.example.backend.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.backend.entity.MatchHistory;
import org.example.backend.repository.IMatchHistoryRepository;
import org.example.backend.service.IMatchHistoryService;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MatchHistoryService implements IMatchHistoryService {
    private final IMatchHistoryRepository matchHistoryRepository;

    @Override
    public List<MatchHistory> findAll() {
        return matchHistoryRepository.findAll();
    }

    @Override
    public Optional<MatchHistory> findById(Long id) {
        return matchHistoryRepository.findById(id);
    }

    @Override
    public MatchHistory save(MatchHistory matchHistory) {
        return matchHistoryRepository.save(matchHistory);
    }

    @Override
    public List<MatchHistory> findByRoomId(Long roomId) {
        return matchHistoryRepository.findByRoomId(roomId);
    }
}

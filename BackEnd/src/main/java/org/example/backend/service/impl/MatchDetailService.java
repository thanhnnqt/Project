package org.example.backend.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.backend.entity.MatchDetail;
import org.example.backend.repository.IMatchDetailRepository;
import org.example.backend.service.IMatchDetailService;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MatchDetailService implements IMatchDetailService {
    private final IMatchDetailRepository matchDetailRepository;

    @Override
    public List<MatchDetail> findByMatchHistoryId(Long matchHistoryId) {
        return matchDetailRepository.findByMatchHistoryId(matchHistoryId);
    }

    @Override
    public List<MatchDetail> findByPlayerId(Long playerId) {
        return matchDetailRepository.findByPlayerId(playerId);
    }

    @Override
    public MatchDetail save(MatchDetail matchDetail) {
        return matchDetailRepository.save(matchDetail);
    }
}

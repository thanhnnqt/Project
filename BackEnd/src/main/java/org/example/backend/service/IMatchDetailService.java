package org.example.backend.service;

import org.example.backend.entity.MatchDetail;
import java.util.List;

public interface IMatchDetailService {
    List<MatchDetail> findByMatchHistoryId(Long matchHistoryId);
    List<MatchDetail> findByPlayerId(Long playerId);
    MatchDetail save(MatchDetail matchDetail);
}

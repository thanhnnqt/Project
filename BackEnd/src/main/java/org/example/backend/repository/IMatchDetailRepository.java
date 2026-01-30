package org.example.backend.repository;

import org.example.backend.entity.MatchDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface IMatchDetailRepository extends JpaRepository<MatchDetail, Long> {
    List<MatchDetail> findByMatchHistoryId(Long matchHistoryId);
    List<MatchDetail> findByPlayerId(Long playerId);
}

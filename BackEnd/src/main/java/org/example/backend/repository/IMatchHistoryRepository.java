package org.example.backend.repository;

import org.example.backend.entity.MatchHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface IMatchHistoryRepository extends JpaRepository<MatchHistory, Long> {
    List<MatchHistory> findByRoomId(Long roomId);
}

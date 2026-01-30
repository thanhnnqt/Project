package org.example.backend.repository;

import org.example.backend.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface IRoomRepository extends JpaRepository<Room, Long> {
    List<Room> findByStatus(String status);
    @org.springframework.data.jpa.repository.Query("SELECT r FROM Room r JOIN FETCH r.gameType JOIN FETCH r.host")
    List<Room> findAll();

    @org.springframework.data.jpa.repository.Query("SELECT r FROM Room r JOIN FETCH r.gameType JOIN FETCH r.host WHERE r.gameType.id = :gameTypeId")
    List<Room> findByGameTypeId(Long gameTypeId);
}

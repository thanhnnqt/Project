package org.example.backend.service;

import org.example.backend.entity.Room;
import java.util.List;
import java.util.Optional;

public interface IRoomService {
    List<Room> findAll();
    Optional<Room> findById(Long id);
    Room save(Room room);
    void deleteById(Long id);
    List<Room> findByStatus(String status);
    List<Room> findByGameType(Long gameTypeId);
}

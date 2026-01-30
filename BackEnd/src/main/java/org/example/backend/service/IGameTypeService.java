package org.example.backend.service;

import org.example.backend.entity.GameType;
import java.util.List;
import java.util.Optional;

public interface IGameTypeService {
    List<GameType> findAll();
    Optional<GameType> findById(Long id);
    GameType save(GameType gameType);
    void deleteById(Long id);
}

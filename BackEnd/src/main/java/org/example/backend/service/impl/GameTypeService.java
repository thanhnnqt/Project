package org.example.backend.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.backend.entity.GameType;
import org.example.backend.repository.IGameTypeRepository;
import org.example.backend.service.IGameTypeService;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GameTypeService implements IGameTypeService {
    private final IGameTypeRepository gameTypeRepository;

    @Override
    public List<GameType> findAll() {
        return gameTypeRepository.findAll();
    }

    @Override
    public Optional<GameType> findById(Long id) {
        return gameTypeRepository.findById(id);
    }

    @Override
    public GameType save(GameType gameType) {
        return gameTypeRepository.save(gameType);
    }

    @Override
    public void deleteById(Long id) {
        gameTypeRepository.deleteById(id);
    }
}

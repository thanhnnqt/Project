package org.example.backend.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.backend.entity.Player;
import org.example.backend.repository.IPlayerRepository;
import org.example.backend.service.IPlayerService;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PlayerService implements IPlayerService {
    private final IPlayerRepository playerRepository;

    @Override
    public List<Player> findAll() {
        return playerRepository.findAll();
    }

    @Override
    public Optional<Player> findById(Long id) {
        return playerRepository.findById(id);
    }

    @Override
    public Player save(Player player) {
        return playerRepository.save(player);
    }

    @Override
    public void deleteById(Long id) {
        playerRepository.deleteById(id);
    }

    @Override
    public Optional<Player> findByUsername(String username) {
        return playerRepository.findByUsername(username);
    }

    @Override
    public List<Player> getTopPlayers(int limit) {
        return playerRepository.findAllByOrderByRankPointsDesc(PageRequest.of(0, limit));
    }
}

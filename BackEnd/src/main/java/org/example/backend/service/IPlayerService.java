package org.example.backend.service;

import org.example.backend.entity.Player;
import java.util.List;
import java.util.Optional;

public interface IPlayerService {
    List<Player> findAll();
    Optional<Player> findById(Long id);
    Player save(Player player);
    void deleteById(Long id);
    Optional<Player> findByUsername(String username);
    List<Player> getTopPlayers(int limit);
}

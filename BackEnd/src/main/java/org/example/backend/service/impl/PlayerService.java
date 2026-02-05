package org.example.backend.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.backend.entity.Player;
import org.example.backend.repository.IPlayerRepository;
import org.example.backend.service.IPlayerService;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlayerService implements IPlayerService {
    private final IPlayerRepository playerRepository;

    // Rank tier order for sorting (higher index = higher rank)
    private static final List<String> RANK_ORDER = Arrays.asList(
        "Sắt IV", "Sắt III", "Sắt II", "Sắt I",
        "Đồng IV", "Đồng III", "Đồng II", "Đồng I",
        "Bạc IV", "Bạc III", "Bạc II", "Bạc I",
        "Vàng IV", "Vàng III", "Vàng II", "Vàng I",
        "Bạch Kim IV", "Bạch Kim III", "Bạch Kim II", "Bạch Kim I",
        "Kim Cương IV", "Kim Cương III", "Kim Cương II", "Kim Cương I",
        "Cao Thủ", "Thách Đấu"
    );

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
        System.out.println("Fetching top players with limit: " + limit);
        List<Player> allPlayers = playerRepository.findAll();
        
        List<Player> sorted = allPlayers.stream()
                .sorted((p1, p2) -> {
                    String tier1 = p1.getRankTier() != null ? p1.getRankTier().trim() : "Sắt IV";
                    String tier2 = p2.getRankTier() != null ? p2.getRankTier().trim() : "Sắt IV";
                    
                    int r1 = RANK_ORDER.indexOf(tier1);
                    int r2 = RANK_ORDER.indexOf(tier2);

                    // Debug if rank not found
                    if (r1 == -1) System.out.println("Warning: Rank not found for player " + p1.getUsername() + ": [" + tier1 + "]");
                    if (r2 == -1) System.out.println("Warning: Rank not found for player " + p2.getUsername() + ": [" + tier2 + "]");

                    // Compare ranks first (descending)
                    if (r1 != r2) {
                        return Integer.compare(r2, r1);
                    }

                    // If ranks are equal, compare points (descending)
                    int pts1 = p1.getRankPoints() != null ? p1.getRankPoints() : 0;
                    int pts2 = p2.getRankPoints() != null ? p2.getRankPoints() : 0;
                    return Integer.compare(pts2, pts1);
                })
                .limit(limit)
                .collect(Collectors.toList());

        System.out.println("Leaderboard result: " + sorted.stream()
            .map(p -> p.getUsername() + "(" + p.getRankTier() + ":" + p.getRankPoints() + ")")
            .collect(Collectors.joining(", ")));
            
        return sorted;
    }
}

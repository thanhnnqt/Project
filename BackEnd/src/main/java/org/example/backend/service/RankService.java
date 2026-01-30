package org.example.backend.service;

import org.example.backend.entity.Player;
import org.springframework.stereotype.Service;
import java.util.Arrays;
import java.util.List;

@Service
public class RankService {
    private static final List<String> RANKS = Arrays.asList(
        "Sắt IV", "Sắt III", "Sắt II", "Sắt I",
        "Đồng IV", "Đồng III", "Đồng II", "Đồng I",
        "Bạc IV", "Bạc III", "Bạc II", "Bạc I",
        "Vàng IV", "Vàng III", "Vàng II", "Vàng I",
        "Bạch Kim IV", "Bạch Kim III", "Bạch Kim II", "Bạch Kim I",
        "Kim Cương IV", "Kim Cương III", "Kim Cương II", "Kim Cương I",
        "Cao Thủ", "Thách Đấu"
    );

    public void processWin(Player player) {
        if (player.getRankTier() == null) player.setRankTier("Sắt IV");
        if (player.getRankPoints() == null) player.setRankPoints(0);

        int currentRankIndex = RANKS.indexOf(player.getRankTier());
        if (currentRankIndex == -1) currentRankIndex = 0;

        // Calculate points to add (60 down to 30)
        double scale = (double) currentRankIndex / (RANKS.size() - 1);
        int pointsToAdd = (int) (60 - (scale * 30));

        int newPoints = player.getRankPoints() + pointsToAdd;

        // Promotion logic
        if (newPoints >= 100 && currentRankIndex < RANKS.size() - 1) {
            newPoints -= 100;
            player.setRankTier(RANKS.get(currentRankIndex + 1));
        }

        player.setRankPoints(newPoints);
    }

    public void processLoss(Player player) {
        if (player.getRankTier() == null) player.setRankTier("Sắt IV");
        if (player.getRankPoints() == null) player.setRankPoints(0);

        int currentRankIndex = RANKS.indexOf(player.getRankTier());
        if (currentRankIndex == -1) currentRankIndex = 0;

        // Subtract points (15 down to 10 as rank goes up)
        double scale = (double) currentRankIndex / (RANKS.size() - 1);
        int pointsToSubtract = (int) (15 - (scale * 5));

        int newPoints = player.getRankPoints() - pointsToSubtract;

        // Demotion logic (optional, let's keep it simple: min 0 points, but no rank down for now unless points < 0)
        if (newPoints < 0) {
            if (currentRankIndex > 0) {
                newPoints = 100 + newPoints; // e.g. 0 - 10 = 90 in lower rank
                player.setRankTier(RANKS.get(currentRankIndex - 1));
            } else {
                newPoints = 0;
            }
        }

        player.setRankPoints(newPoints);
    }
}

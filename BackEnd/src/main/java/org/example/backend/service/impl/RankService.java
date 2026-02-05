package org.example.backend.service.impl;

import org.example.backend.entity.Player;
import org.springframework.stereotype.Service;
import java.util.Arrays;
import java.util.List;

@Service
public class RankService {
    public static final List<String> RANKS = Arrays.asList(
        "Sắt IV", "Sắt III", "Sắt II", "Sắt I",
        "Đồng IV", "Đồng III", "Đồng II", "Đồng I",
        "Bạc IV", "Bạc III", "Bạc II", "Bạc I",
        "Vàng IV", "Vàng III", "Vàng II", "Vàng I",
        "Bạch Kim IV", "Bạch Kim III", "Bạch Kim II", "Bạch Kim I",
        "Kim Cương IV", "Kim Cương III", "Kim Cương II", "Kim Cương I",
        "Cao Thủ", "Thách Đấu"
    );

    public static int getGlobalPoints(Player player) {
        if (player == null) return 0;
        int tierIndex = RANKS.indexOf(player.getRankTier());
        if (tierIndex == -1) tierIndex = 0;
        return (tierIndex * 100) + (player.getRankPoints() != null ? player.getRankPoints() : 0);
    }

    public java.math.BigDecimal processWin(Player player) {
        if (player.getRankTier() == null) player.setRankTier("Sắt IV");
        if (player.getRankPoints() == null) player.setRankPoints(0);

        int currentRankIndex = RANKS.indexOf(player.getRankTier());
        if (currentRankIndex == -1) currentRankIndex = 0;

        // Calculate points to add (60 down to 30)
        double scale = (double) currentRankIndex / (RANKS.size() - 1);
        int pointsToAdd = (int) (60 - (scale * 30));

        int newPoints = player.getRankPoints() + pointsToAdd;
        java.math.BigDecimal reward = java.math.BigDecimal.ZERO;

        // Promotion logic
        if (newPoints >= 100 && currentRankIndex < RANKS.size() - 1) {
            newPoints -= 100;
            player.setRankTier(RANKS.get(currentRankIndex + 1));
            
            // Promotion Reward (100 to 5000 based on new rank index)
            int newRankIndex = currentRankIndex + 1;
            double rewardScale = (double) newRankIndex / (RANKS.size() - 1);
            int rewardAmount = (int) (100 + (rewardScale * 4900));
            reward = new java.math.BigDecimal(rewardAmount);
        }

        player.setRankPoints(newPoints);
        return reward;
    }

    public void processLoss(Player player) {
        if (player.getRankTier() == null) player.setRankTier("Sắt IV");
        if (player.getRankPoints() == null) player.setRankPoints(0);

        int currentRankIndex = RANKS.indexOf(player.getRankTier());
        if (currentRankIndex == -1) currentRankIndex = 0;

        // Subtract points (20 down to 10 as rank goes up)
        double scale = (double) currentRankIndex / (RANKS.size() - 1);
        int pointsToSubtract = (int) (20 - (scale * 10));

        int newPoints = player.getRankPoints() - pointsToSubtract;

        // Demotion protection: cannot drop below 0 of current rank tier
        if (newPoints < 0) {
            newPoints = 0;
        }

        player.setRankPoints(newPoints);
    }
}

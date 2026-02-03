package org.example.backend.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.entity.Player;
import org.example.backend.service.IPlayerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leaderboard")
@RequiredArgsConstructor
public class LeaderboardController {
    private final IPlayerService playerService;

    @GetMapping
    public ResponseEntity<List<Player>> getLeaderboard(
            @RequestParam(defaultValue = "10") int limit) {
        List<Player> topPlayers = playerService.getTopPlayers(limit);
        return ResponseEntity.ok(topPlayers);
    }
}

package org.example.backend.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.entity.GameType;
import org.example.backend.service.IGameTypeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/game-types")
@RequiredArgsConstructor
public class GameTypeController {
    private final IGameTypeService gameTypeService;

    @GetMapping
    public ResponseEntity<List<GameType>> getAllGameTypes() {
        return ResponseEntity.ok(gameTypeService.findAll());
    }
}

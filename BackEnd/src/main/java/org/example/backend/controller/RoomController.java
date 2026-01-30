package org.example.backend.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.dto.CreateRoomRequest;
import org.example.backend.dto.RoomResponse;
import org.example.backend.entity.GameType;
import org.example.backend.entity.Player;
import org.example.backend.entity.Room;
import org.example.backend.game.GameManager;
import org.example.backend.service.IGameTypeService;
import org.example.backend.service.IPlayerService;
import org.example.backend.service.IRoomService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomController {
    private final IRoomService roomService;
    private final IGameTypeService gameTypeService;
    private final IPlayerService playerService;
    private final GameManager gameManager;

    @GetMapping
    public ResponseEntity<List<RoomResponse>> getAllRooms(@RequestParam(required = false) Long gameTypeId) {
        List<Room> rooms;
        if (gameTypeId != null) {
            rooms = roomService.findByGameType(gameTypeId);
        } else {
            rooms = roomService.findAll();
        }

        List<RoomResponse> responses = rooms.stream().map(room -> {
            int count = gameManager.getOrCreateGame(String.valueOf(room.getId())).getPlayerIds().size();
            return RoomResponse.fromEntity(room, count);
        }).collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @PostMapping
    public ResponseEntity<?> createRoom(@RequestBody CreateRoomRequest request) {
        GameType gameType = gameTypeService.findById(request.getGameTypeId()).orElse(null);
        Player host = playerService.findById(request.getHostId()).orElse(null);

        if (gameType == null || host == null) {
            return ResponseEntity.badRequest().body("Game type hoặc Player không tồn tại");
        }

        Room room = new Room();
        room.setName(request.getName());
        room.setGameType(gameType);
        room.setMinBet(request.getMinBet());
        room.setHost(host);
        room.setPassword(request.getPassword());
        room.setStatus("WAITING");
        room.setCreatedAt(LocalDateTime.now());

        Room savedRoom = roomService.save(room);
        
        // Return as Response
        return ResponseEntity.status(HttpStatus.CREATED).body(RoomResponse.fromEntity(savedRoom, 0));
    }
}

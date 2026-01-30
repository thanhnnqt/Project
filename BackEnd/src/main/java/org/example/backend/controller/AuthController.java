package org.example.backend.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.dto.LoginRequest;
import org.example.backend.dto.RegisterRequest;
import org.example.backend.entity.Player;
import org.example.backend.service.IPlayerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final IPlayerService playerService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        // Basic validation
        if (request.getUsername() == null || request.getPassword() == null) {
            return ResponseEntity.badRequest().body("Username và password không được để trống");
        }

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            return ResponseEntity.badRequest().body("Mật khẩu xác nhận không khớp");
        }

        if (playerService.findByUsername(request.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body("Tên đăng nhập đã tồn tại");
        }

        Player player = new Player();
        player.setDisplayName(request.getDisplayName());
        player.setUsername(request.getUsername());
        player.setPassword(request.getPassword()); // In real app, must encode password
        player.setEmail(request.getEmail());
        player.setAge(request.getAge());
        player.setPhoneNumber(request.getPhoneNumber());
        player.setBalance(new BigDecimal("1000.00")); // Starting bonus
        player.setStatus("OFFLINE");

        playerService.save(player);

        return ResponseEntity.status(HttpStatus.CREATED).body("Đăng ký thành công!");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        if (request.getUsername() == null || request.getPassword() == null) {
            return ResponseEntity.badRequest().body("Username và password không được để trống");
        }

        return playerService.findByUsername(request.getUsername())
                .map(player -> {
                    if (player.getPassword().equals(request.getPassword())) {
                        return ResponseEntity.ok(player); // In real app, return JWT
                    } else {
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Sai mật khẩu");
                    }
                })
                .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Tên đăng nhập không tồn tại"));
    }

    @RequestMapping("/profile/{id}")
    public ResponseEntity<?> getProfile(@PathVariable Long id) {
        return playerService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}

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

import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.UUID;
import java.io.File;

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

    @PostMapping("/upload-avatar/{id}")
    public ResponseEntity<?> uploadAvatar(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        try {
            Optional<Player> playerOpt = playerService.findById(id);
            if (playerOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("File không được trống");
            }

            // Create upload directory if not exists
            String uploadDir = System.getProperty("user.dir") + "/uploads/avatars/";
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String fileName = UUID.randomUUID().toString() + extension;
            Path filePath = Paths.get(uploadDir + fileName);

            // Save file
            Files.copy(file.getInputStream(), filePath);

            // Update player avatar URL
            Player player = playerOpt.get();
            // The URL will be served via WebConfig mapping /api/avatars/** to uploads/avatars/
            String avatarUrl = "/api/avatars/" + fileName;
            player.setAvatar(avatarUrl);
            playerService.save(player);

            return ResponseEntity.ok(player);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi tải ảnh lên: " + e.getMessage());
        }
    }
}

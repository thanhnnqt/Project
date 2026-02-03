package org.example.backend.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.entity.Player;
import org.example.backend.entity.PlayerInventory;
import org.example.backend.entity.ShopItem;
import org.example.backend.repository.IPlayerInventoryRepository;
import org.example.backend.repository.IPlayerRepository;
import org.example.backend.repository.IShopItemRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/shop")
@RequiredArgsConstructor
public class ShopController {
    private final IShopItemRepository shopItemRepository;
    private final IPlayerInventoryRepository inventoryRepository;
    private final IPlayerRepository playerRepository;

    @GetMapping("/items")
    public ResponseEntity<List<ShopItem>> getAllItems() {
        return ResponseEntity.ok(shopItemRepository.findAll());
    }

    @GetMapping("/items/{type}")
    public ResponseEntity<List<ShopItem>> getItemsByType(@PathVariable String type) {
        return ResponseEntity.ok(shopItemRepository.findByType(type));
    }

    @GetMapping("/inventory/{playerId}")
    public ResponseEntity<List<PlayerInventory>> getPlayerInventory(@PathVariable Long playerId) {
        return ResponseEntity.ok(inventoryRepository.findByPlayerId(playerId));
    }

    @PostMapping("/purchase")
    public ResponseEntity<?> purchaseItem(@RequestBody Map<String, Long> request) {
        Long playerId = request.get("playerId");
        Long itemId = request.get("itemId");

        // Check if player exists
        Player player = playerRepository.findById(playerId)
                .orElse(null);
        if (player == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Người chơi không tồn tại");
        }

        // Check if item exists
        ShopItem item = shopItemRepository.findById(itemId)
                .orElse(null);
        if (item == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Vật phẩm không tồn tại");
        }

        // Check if already owned
        if (inventoryRepository.existsByPlayerIdAndShopItemId(playerId, itemId)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Bạn đã sở hữu vật phẩm này");
        }

        // Check if player has enough balance
        if (player.getBalance().compareTo(item.getPrice()) < 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Không đủ xu để mua");
        }

        // Deduct balance
        player.setBalance(player.getBalance().subtract(item.getPrice()));
        playerRepository.save(player);

        // Add to inventory
        PlayerInventory inventory = new PlayerInventory();
        inventory.setPlayer(player);
        inventory.setShopItem(item);
        inventoryRepository.save(inventory);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Mua thành công!");
        response.put("newBalance", player.getBalance());
        return ResponseEntity.ok(response);
    }
}

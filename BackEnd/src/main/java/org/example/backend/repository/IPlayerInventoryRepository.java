package org.example.backend.repository;

import org.example.backend.entity.PlayerInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface IPlayerInventoryRepository extends JpaRepository<PlayerInventory, Long> {
    List<PlayerInventory> findByPlayerId(Long playerId);
    Optional<PlayerInventory> findByPlayerIdAndShopItemId(Long playerId, Long shopItemId);
    boolean existsByPlayerIdAndShopItemId(Long playerId, Long shopItemId);
    
    @org.springframework.data.jpa.repository.Query("SELECT pi FROM PlayerInventory pi WHERE pi.player.id = :playerId AND pi.isEquipped = true AND pi.shopItem.type = :type")
    java.util.Optional<PlayerInventory> findEquippedItemByType(Long playerId, String type);
}

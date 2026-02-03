package org.example.backend.repository;

import org.example.backend.entity.ShopItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface IShopItemRepository extends JpaRepository<ShopItem, Long> {
    List<ShopItem> findByType(String type);
}

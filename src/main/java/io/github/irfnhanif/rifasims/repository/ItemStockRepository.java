package io.github.irfnhanif.rifasims.repository;

import io.github.irfnhanif.rifasims.entity.Item;
import io.github.irfnhanif.rifasims.entity.ItemStock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ItemStockRepository extends JpaRepository<ItemStock, UUID> {
    Optional<ItemStock> findByItem(Item item);

    @Query("SELECT is FROM ItemStock is WHERE is.currentStock < is.threshold")
    Page<ItemStock> findItemStocksBelowThreshold(Pageable pageable);
    Page<ItemStock> findByItem_NameContaining(String itemName, Pageable pageable);
}

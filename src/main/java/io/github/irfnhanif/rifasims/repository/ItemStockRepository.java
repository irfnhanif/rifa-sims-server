package io.github.irfnhanif.rifasims.repository;

import io.github.irfnhanif.rifasims.entity.Item;
import io.github.irfnhanif.rifasims.entity.ItemStock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ItemStockRepository extends JpaRepository<ItemStock, UUID> {
    Optional<ItemStock> findByItem(Item item);
    List<ItemStock> findByCurrentStockLessThan(Integer threshold);

    Page<ItemStock> findByItem_NameContaining(String itemName, Pageable pageable);
}

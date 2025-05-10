package io.github.irfnhanif.rifasims.repository;

import io.github.irfnhanif.rifasims.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ItemRepository  extends JpaRepository<Item, UUID> {
    List<Item> findByBarcode(String barcode);
    List<Item> findByNameContaining(String name);
    Optional<Item> findById(UUID id);
}

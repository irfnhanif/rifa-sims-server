package io.github.irfnhanif.rifasims.repository;

import io.github.irfnhanif.rifasims.entity.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.net.URLConnection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ItemRepository  extends JpaRepository<Item, UUID> {
    List<Item> findByBarcode(String barcode);
    Optional<Item> findById(UUID id);
    Page<Item> findByNameContainingIgnoreCase(String name, Pageable pageable);
}

package io.github.irfnhanif.rifasims.repository;

import io.github.irfnhanif.rifasims.entity.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ItemRepository  extends JpaRepository<Item, UUID> {
    Optional<Item> findByIdAndDeletedFalse(UUID id);

    Page<Item> findByNameContainingIgnoreCaseAndDeletedFalse(String name, Pageable pageable);

    Optional<Item> findByName(String name);

    Page<Item> findByDeletedFalse(Pageable pageable);
}

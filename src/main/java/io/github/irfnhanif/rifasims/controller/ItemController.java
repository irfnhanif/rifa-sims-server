package io.github.irfnhanif.rifasims.controller;

import io.github.irfnhanif.rifasims.entity.Item;
import io.github.irfnhanif.rifasims.entity.StockChangeType;
import io.github.irfnhanif.rifasims.repository.ItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/items")
public class ItemController {

    @Autowired
    private ItemRepository itemRepository;

    public ItemController(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    @PreAuthorize("hasRole('OWNER')")
    @PostMapping("")
    public ResponseEntity<Item> createItem(@RequestBody Item item) {

    }

    @GetMapping("")
    public ResponseEntity<List<Item>> getAllItems(@RequestParam(required = false) String name) {

    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Item> getItem(@PathVariable UUID itemId) {

    }

    @GetMapping("/{itemId}/history")
    public ResponseEntity<List<Item>> getItemHistory(@PathVariable UUID itemId,
                                                     @RequestParam(required = false) LocalDateTime startDate, @RequestParam(required = false) LocalDateTime endDate,
                                                     @RequestParam(required = false) StockChangeType type) {

    }

    @GetMapping("/barcode/{barcode}")
    public ResponseEntity<List<Item>> getItemByBarcode(@RequestParam String barcode) {

    }

    @PreAuthorize("hasRole('OWNER')")
    @PutMapping("/{itemId}")
    public ResponseEntity<Item> updateItem(@PathVariable UUID itemId, @RequestBody Item item) {

    }

    @PreAuthorize("hasRole('OWNER')")
    @DeleteMapping("/{itemId}")
    public ResponseEntity<Item> deleteItem(@PathVariable UUID itemId) {

    }
}

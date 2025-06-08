package io.github.irfnhanif.rifasims.controller;

import io.github.irfnhanif.rifasims.dto.APIResponse;
import io.github.irfnhanif.rifasims.dto.CreateItemRequest;
import io.github.irfnhanif.rifasims.dto.ItemDetailResponse;
import io.github.irfnhanif.rifasims.entity.Item;
import io.github.irfnhanif.rifasims.exception.InternalServerException;
import io.github.irfnhanif.rifasims.service.ItemService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/items")
public class ItemController {

    private ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @PreAuthorize("hasAuthority('OWNER')")
    @PostMapping("")
    public ResponseEntity<APIResponse<Item>> createItem(@Valid  @RequestBody CreateItemRequest createItemRequest) {
        Item createdItem = itemService.createItem(createItemRequest);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new APIResponse<>(true, "Barang berhasil dibuat", createdItem, null));
    }

    @GetMapping("")
    public ResponseEntity<APIResponse<List<Item>>> getAllItems(@RequestParam(required = false) String name,
                                                               @RequestParam(required = false, defaultValue = "0") Integer page,
                                                               @RequestParam(required = false, defaultValue = "10") Integer size) {
        List<Item> items = itemService.getAllItems(name, page, size);
        return ResponseEntity.ok(new APIResponse<>(true, "Barang berhasil diambil", items, null));
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<APIResponse<Item>> getItem(@PathVariable UUID itemId) {
        Item item = itemService.getItemById(itemId);
        return ResponseEntity.ok(new APIResponse<>(true, "Barang berhasil diambil", item, null));
    }

    @GetMapping("/{itemId}/detail")
    public ResponseEntity<APIResponse<ItemDetailResponse>> getItem(@PathVariable UUID itemId, @RequestParam(required = false) LocalDateTime fromDate, @RequestParam(required = false) LocalDateTime toDate) {
        LocalDateTime checkedToDate = toDate != null ? toDate : LocalDateTime.now();
        LocalDateTime checkedFromDate = fromDate != null ? fromDate : checkedToDate.minusWeeks(1);

        ItemDetailResponse response = itemService.getItemById(itemId, checkedFromDate, checkedToDate);
        return ResponseEntity.ok(new APIResponse<>(true, "Detail barang berhasil diambil", response, null));
    }


    @PreAuthorize("hasAuthority('OWNER')")
    @PutMapping("/{itemId}")
    public ResponseEntity<APIResponse<Item>> updateItem(@PathVariable UUID itemId, @Valid @RequestBody Item item) {
        Item updatedItem = itemService.updateItem(itemId, item);
        return ResponseEntity.ok(new APIResponse<>(true, "Barang berhasil diperbarui", updatedItem, null));
    }

    @PreAuthorize("hasAuthority('OWNER')")
    @DeleteMapping("/{itemId}")
    public ResponseEntity<APIResponse<Void>> deleteItem(@PathVariable UUID itemId) {
        itemService.deleteItem(itemId);
        return ResponseEntity.ok(new APIResponse<>(true, "Barang berhasil dihapus", null, null));
    }
}
package io.github.irfnhanif.rifasims.controller;

import io.github.irfnhanif.rifasims.dto.APIResponse;
import io.github.irfnhanif.rifasims.dto.BarcodeScanResponse;
import io.github.irfnhanif.rifasims.dto.CreateItemRequest;
import io.github.irfnhanif.rifasims.entity.Item;
import io.github.irfnhanif.rifasims.entity.StockAuditLog;
import io.github.irfnhanif.rifasims.entity.StockChangeType;
import io.github.irfnhanif.rifasims.exception.BadRequestException;
import io.github.irfnhanif.rifasims.exception.InternalServerException;
import io.github.irfnhanif.rifasims.service.ItemService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

//    @PreAuthorize("hasRole('OWNER')")
    @PostMapping("")
    public ResponseEntity<APIResponse<Item>> createItem(@Valid  @RequestBody CreateItemRequest createItemRequest) {
        try {
            Item createdItem = itemService.createItem(createItemRequest);
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(new APIResponse<>(true, "Item created successfully", createdItem, null));
        } catch (Exception e) {
            throw new InternalServerException(e.getMessage());
        }
    }

    @GetMapping("")
    public ResponseEntity<APIResponse<List<Item>>> getAllItems(@RequestParam(required = false) String name,
                                                               @RequestParam(required = false, defaultValue = "0") Integer page,
                                                               @RequestParam(required = false, defaultValue = "10") Integer size) {
        try {
            List<Item> items = itemService.getAllItems(name, page, size);
            return ResponseEntity.ok(new APIResponse<>(true, "Items retrieved successfully", items, null));
        } catch (Exception e) {
            throw new InternalServerException(e.getMessage());
        }
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<APIResponse<Item>> getItem(@PathVariable UUID itemId) {
        try {
            Item item = itemService.getItemById(itemId);
            return ResponseEntity.ok(new APIResponse<>(true, "Item retrieved successfully", item, null));
        } catch (Exception e) {
            throw new InternalServerException(e.getMessage());
        }
    }

    @GetMapping("/{itemId}/history")
    public ResponseEntity<APIResponse<List<StockAuditLog>>> getItemHistory(@PathVariable UUID itemId,
                                                                           @RequestParam(required = false) LocalDateTime startDate, @RequestParam(required = false) LocalDateTime endDate,
                                                                           @RequestParam(required = false) StockChangeType type) {
        try {
            List<StockAuditLog> itemStockHistory = itemService.getStockAuditLogsByItemId(itemId);
            return ResponseEntity.ok(new APIResponse<>(true, "Stock History retrieved successfully", itemStockHistory, null));
        } catch (Exception e) {
            throw new InternalServerException(e.getMessage());
        }
    }

    @GetMapping("/barcode/{barcode}")
    public ResponseEntity<APIResponse<List<BarcodeScanResponse>>> getItemByBarcode(@PathVariable String barcode) {
        try {
            if (barcode == null || barcode.isEmpty()) {
                throw new BadRequestException("Barcode cannot be empty");
            }

            List<BarcodeScanResponse> responses = itemService.getItemsByBarcode(barcode);
            return ResponseEntity.ok(new APIResponse<>(true, "Items retrieved successfully", responses, null));
        } catch (Exception e) {
            throw new InternalServerException(e.getMessage());
        }
    }

//    @PreAuthorize("hasRole('OWNER')")
    @PutMapping("/{itemId}")
    public ResponseEntity<APIResponse<Item>> updateItem(@PathVariable UUID itemId, @Valid @RequestBody Item item) {
        try {
            Item updatedItem = itemService.updateItem(itemId, item);
            return ResponseEntity.ok(new APIResponse<>(true, "Item updated successfully", updatedItem, null));
        } catch (Exception e) {
            throw new InternalServerException(e.getMessage());
        }
    }

//    @PreAuthorize("hasRole('OWNER')")
    @DeleteMapping("/{itemId}")
    public ResponseEntity<APIResponse<Void>> deleteItem(@PathVariable UUID itemId) {
        try {
            itemService.deleteItem(itemId);
            return ResponseEntity.ok(new APIResponse<>(true, "Item deleted successfully", null, null));
        } catch (Exception e) {
            throw new InternalServerException(e.getMessage());
        }
    }
}

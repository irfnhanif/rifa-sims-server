package io.github.irfnhanif.rifasims.controller;

import io.github.irfnhanif.rifasims.dto.APIResponse;
import io.github.irfnhanif.rifasims.dto.EditStockChangeRequest;
import io.github.irfnhanif.rifasims.dto.ScanStockChangeRequest;
import io.github.irfnhanif.rifasims.entity.ItemStock;
import io.github.irfnhanif.rifasims.exception.InternalServerException;
import io.github.irfnhanif.rifasims.service.ItemStockService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/item-stocks")
public class ItemStockController {


    private final ItemStockService itemStockService;

    public ItemStockController(ItemStockService itemStockService) {
        this.itemStockService = itemStockService;
    }

    @GetMapping("")
    public ResponseEntity<APIResponse<List<ItemStock>>> getItemStocks(@RequestParam(required = false) String name, @RequestParam(required = false, defaultValue = "0") Integer page, @RequestParam(required = false, defaultValue = "10") Integer size) {
        try {
            List<ItemStock> itemStocks = itemStockService.getAllItemStocks(name, page, size);
            return ResponseEntity.ok(new APIResponse<>(true, "Item stocks retrieved successfully", itemStocks, null));
        } catch (Exception e) {
            throw new InternalServerException(e.getMessage());
        }
    }

    @GetMapping("/near-empty")
    public ResponseEntity<APIResponse<List<ItemStock>>> getItemStocksLessThanThreshold(@RequestParam(required = false, defaultValue = "0") Integer page, @RequestParam(required = false, defaultValue = "10") Integer size) {
        try {
            List<ItemStock> itemStocks = itemStockService.getItemStocksLessThanThreshold(page, size);
            return ResponseEntity.ok(new APIResponse<>(true, "Near-empty item stocks retrieved successfully", itemStocks, null));
        } catch (Exception e) {
            throw new InternalServerException(e.getMessage());
        }
    }

    @GetMapping("/{itemStockId}")
    public ResponseEntity<APIResponse<ItemStock>> getItemStock(@PathVariable UUID itemStockId) {
        try {
            ItemStock itemStock = itemStockService.getItemStockById(itemStockId);
            return ResponseEntity.ok(new APIResponse<>(true, "Item stock retrieved successfully", itemStock, null));
        } catch (Exception e) {
            throw new InternalServerException(e.getMessage());
        }
    }

    @PutMapping("/{itemStockId}")
    public ResponseEntity<APIResponse<ItemStock>> updateItemStock(@PathVariable UUID itemStockId, @RequestBody EditStockChangeRequest editStockChangeRequest) {
        try {
            ItemStock updatedItemStock = itemStockService.updateItemStockChange(itemStockId, editStockChangeRequest);
            return ResponseEntity.ok(new APIResponse<>(true, "Item stock updated successfully", updatedItemStock, null));
        } catch (Exception e) {
            throw new InternalServerException(e.getMessage());
        }
    }

    @PatchMapping("/{itemStockId}/scan")
    public ResponseEntity<APIResponse<ItemStock>> scanItemStock(@PathVariable UUID itemStockId, @RequestBody ScanStockChangeRequest scanStockChangeRequest) {
        try {
            ItemStock scannedItemStock = itemStockService.updateScanItemStockChange(itemStockId, scanStockChangeRequest);
            return ResponseEntity.ok(new APIResponse<>(true, "Item stock scanned successfully", scannedItemStock, null));
        } catch (Exception e) {
            throw new InternalServerException(e.getMessage());
        }
    }

    @DeleteMapping("/{itemStockId}")
    public ResponseEntity<APIResponse<Void>> deleteItemStock(@PathVariable UUID itemStockId) {
        try {
            itemStockService.deleteItemStockChange(itemStockId);
            return ResponseEntity.ok(new APIResponse<>(true, "Item deleted successfully", null, null));
        } catch (Exception e) {
            throw new InternalServerException(e.getMessage());
        }
    }
}

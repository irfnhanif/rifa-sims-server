package io.github.irfnhanif.rifasims.controller;

import io.github.irfnhanif.rifasims.dto.APIResponse;
import io.github.irfnhanif.rifasims.dto.StockScanRequest;
import io.github.irfnhanif.rifasims.entity.ItemStock;
import io.github.irfnhanif.rifasims.exception.InternalServerException;
import io.github.irfnhanif.rifasims.service.ItemStockService;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
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
    public APIResponse<List<ItemStock>> getItemStocks(@RequestParam String name, @RequestParam(required = false, defaultValue = "0") Integer page, @RequestParam(required = false, defaultValue = "10") Integer size) {
        try {
            List<ItemStock> itemStocks = itemStockService.getAllItemStocks(name, page, size);
            return new APIResponse<>(true, "Item stocks retrieved successfully", itemStocks, null);
        } catch (Exception e) {
            throw new InternalServerException(e.getMessage());
        }
    }

    @GetMapping("/{itemStockId}")
    public ItemStock getItemStock(@PathVariable UUID itemStockId) {

    }

    @PostMapping("")
    public ItemStock addItemStock(@RequestBody ItemStock itemStock) {

    }

    @PutMapping("/{itemStockId}")
    public ItemStock updateItemStock(@PathVariable UUID itemStockId, @RequestBody ItemStock itemStock) {

    }

    @PutMapping("/{itemStockId}/scan")
    public ItemStock scanItemStock(@PathVariable UUID itemStockId, @RequestBody StockScanRequest stockScanRequest) {

    }

    @DeleteMapping("/{itemStockId}")
    public void deleteItemStock(@PathVariable UUID itemStockId) {

    }
}

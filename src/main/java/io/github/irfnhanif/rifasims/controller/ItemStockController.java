package io.github.irfnhanif.rifasims.controller;

import io.github.irfnhanif.rifasims.dto.APIResponse;
import io.github.irfnhanif.rifasims.dto.BarcodeScanResponse;
import io.github.irfnhanif.rifasims.dto.EditStockChangeRequest;
import io.github.irfnhanif.rifasims.dto.ScanStockChangeRequest;
import io.github.irfnhanif.rifasims.entity.ItemStock;
import io.github.irfnhanif.rifasims.exception.BadRequestException;
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
            return ResponseEntity.ok(new APIResponse<>(true, "Stok barang berhasil diambil", itemStocks, null));
        } catch (Exception e) {
            throw new InternalServerException(e.getMessage());
        }
    }

    @GetMapping("/near-empty")
    public ResponseEntity<APIResponse<List<ItemStock>>> getItemStocksLessThanThreshold(@RequestParam(required = false) String name, @RequestParam(required = false, defaultValue = "0") Integer page, @RequestParam(required = false, defaultValue = "10") Integer size) {
        List<ItemStock> itemStocks = itemStockService.getPagedItemStocksBelowThreshold(name, page, size);
        return ResponseEntity.ok(new APIResponse<>(true, "Stok barang hampir habis berhasil diambil", itemStocks, null));
    }

    @GetMapping("/barcode/{barcode}")
    public ResponseEntity<APIResponse<List<BarcodeScanResponse>>> getItemByBarcode(@PathVariable String barcode) {
        if (barcode == null || barcode.isEmpty()) {
            throw new BadRequestException("Barcode tidak boleh kosong");
        }

        List<BarcodeScanResponse> responses = itemStockService.getItemStocksByBarcode(barcode);
        return ResponseEntity.ok(new APIResponse<>(true, "Barang berhasil diambil", responses, null));
    }

    @GetMapping("/{itemStockId}")
    public ResponseEntity<APIResponse<ItemStock>> getItemStock(@PathVariable UUID itemStockId) {
        ItemStock itemStock = itemStockService.getItemStockById(itemStockId);
        return ResponseEntity.ok(new APIResponse<>(true, "Stok barang berhasil diambil", itemStock, null));
    }

    @PutMapping("/{itemStockId}")
    public ResponseEntity<APIResponse<ItemStock>> updateItemStock(@PathVariable UUID itemStockId, @RequestBody EditStockChangeRequest editStockChangeRequest) {
        ItemStock updatedItemStock = itemStockService.updateItemStockChange(itemStockId, editStockChangeRequest);
        return ResponseEntity.ok(new APIResponse<>(true, "Stok barang berhasil diperbarui", updatedItemStock, null));
    }

    @PatchMapping("/{itemStockId}/scan")
    public ResponseEntity<APIResponse<ItemStock>> scanItemStock(@PathVariable UUID itemStockId, @RequestBody ScanStockChangeRequest scanStockChangeRequest) {
        ItemStock scannedItemStock = itemStockService.updateScanItemStockChange(itemStockId, scanStockChangeRequest);
        return ResponseEntity.ok(new APIResponse<>(true, "Stok barang berhasil dipindai", scannedItemStock, null));
    }
}
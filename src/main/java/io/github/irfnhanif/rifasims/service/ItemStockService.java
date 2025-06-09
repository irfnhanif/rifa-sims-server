package io.github.irfnhanif.rifasims.service;

import io.github.irfnhanif.rifasims.dto.BarcodeScanResponse;
import io.github.irfnhanif.rifasims.dto.EditStockChangeRequest;
import io.github.irfnhanif.rifasims.dto.RecommendedBarcodeScanResponse;
import io.github.irfnhanif.rifasims.dto.ScanStockChangeRequest;
import io.github.irfnhanif.rifasims.entity.*;
import io.github.irfnhanif.rifasims.exception.BadRequestException;
import io.github.irfnhanif.rifasims.exception.ResourceNotFoundException;
import io.github.irfnhanif.rifasims.repository.ItemStockRepository;
import io.github.irfnhanif.rifasims.repository.UserRepository;
import io.github.irfnhanif.rifasims.util.ScanItemRecommender;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ItemStockService {

    private final ItemStockRepository itemStockRepository;
    private final StockAuditLogService stockAuditLogService;
    private final UserService userService;
    private final ScanItemRecommender scanItemRecommender = new ScanItemRecommender();

    public ItemStockService(ItemStockRepository itemStockRepository, StockAuditLogService stockAuditLogService, UserService userService) {
        this.itemStockRepository = itemStockRepository;
        this.stockAuditLogService = stockAuditLogService;
        this.userService = userService;
    }

    public List<ItemStock> getAllItemStocks(String name, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page, size);
        if (name != null) {
            return itemStockRepository.findByItem_NameContaining(name, pageable).getContent();
        }
        return itemStockRepository.findAll(pageable).getContent();
    }

    public List<ItemStock> getAllItemStocksBelowThreshold() {
        return itemStockRepository.findAllItemStocksBelowThreshold();
    }

    public List<ItemStock> getPagedItemStocksBelowThreshold(String name, Integer page, Integer size) {
        return itemStockRepository.findItemStocksByNameBelowThreshold(name, PageRequest.of(page, size)).getContent();
    }

    public List<BarcodeScanResponse> getItemStocksByBarcode(String barcode) {
        List<BarcodeScanResponse> responses = itemStockRepository.findItemStocksByBarcode(barcode);
        return responses;
    }

    public List<RecommendedBarcodeScanResponse> getRecommendedItemStocksByBarcode(String barcode) {
        List<ItemStock> itemStocks = itemStockRepository.findByItem_Barcode(barcode);
        List<StockAuditLog> stockAuditLogs = stockAuditLogService.getStockAuditLogsByItemBarcode(barcode);

        return scanItemRecommender.recommendItem(itemStocks, stockAuditLogs);
    }

    public ItemStock getItemStockById(UUID itemStockId) {
        return itemStockRepository.findById(itemStockId).orElseThrow(() -> new ResourceNotFoundException("Item stock not found"));
    }

    public ItemStock createItemStock(ItemStock itemStock) {
        return itemStockRepository.save(itemStock);
    }


    public ItemStock updateItemStockChange(UUID itemStockId, EditStockChangeRequest editStockChangeRequest) {
        ItemStock existingItemStock = itemStockRepository.findById(itemStockId)
                .orElseThrow(() -> new ResourceNotFoundException("Item stock not found"));

        Integer oldStock = existingItemStock.getCurrentStock();

        existingItemStock.setCurrentStock(editStockChangeRequest.getCurrentStock());
        existingItemStock.setThreshold(editStockChangeRequest.getThreshold());
        itemStockRepository.save(existingItemStock);

        stockAuditLogService.recordStockChange(
                existingItemStock.getItem(),
                userService.getCurrentUser(),
                StockChangeType.MANUAL_EDIT,
                oldStock,
                editStockChangeRequest.getCurrentStock(),
                editStockChangeRequest.getReason(),
                LocalDateTime.now()
        );

        return existingItemStock;
    }

    public ItemStock updateScanItemStockChange(UUID itemStockId, ScanStockChangeRequest scanStockChangeRequest) {
        ItemStock itemStock = itemStockRepository.findById(itemStockId).orElseThrow(() -> new ResourceNotFoundException("Item stock not found"));

        Integer oldStock = itemStock.getCurrentStock();
        Integer newStock = 0;

        if (scanStockChangeRequest.getChangeType() == StockChangeType.OUT) {
            newStock = itemStock.getCurrentStock() - scanStockChangeRequest.getAmount();
        } else {
            newStock = itemStock.getCurrentStock() + scanStockChangeRequest.getAmount();
        }

        if (newStock < 0) {
            throw new BadRequestException("Jumlah stok tidak boleh di bawah 0");
        }

        itemStock.setCurrentStock(newStock);
        itemStockRepository.save(itemStock);

        stockAuditLogService.recordStockChange(
                itemStock.getItem(),
                userService.getCurrentUser(),
                scanStockChangeRequest.getChangeType(),
                oldStock,
                newStock,
                null,
                LocalDateTime.now()
        );

        return itemStock;
    }

    public Integer deleteItemStockChange(Item item) {
        ItemStock itemStock = itemStockRepository.findByItem(item).orElseThrow(() -> new ResourceNotFoundException("Item stock not found"));
        Integer oldStock = itemStock.getCurrentStock();
        itemStockRepository.delete(itemStock);
        return oldStock;
    }

    public Map<String, Integer> restoreOldItemStock(String itemName, StockChangeType stockChangeType, Integer difference) {
        ItemStock itemStock = itemStockRepository.findByItem_Name(itemName).orElseThrow(() -> new ResourceNotFoundException("Item not found"));
        Integer originalStock = itemStock.getCurrentStock();
        Integer newStock = 0;

        if (stockChangeType == StockChangeType.IN) {
            itemStock.setCurrentStock(originalStock - difference);
            newStock = itemStock.getCurrentStock();
        } else {
            itemStock.setCurrentStock(originalStock + difference);
            newStock = itemStock.getCurrentStock();
        }
        itemStockRepository.save(itemStock);

        return Map.of("oldStock", originalStock, "newStock", newStock);
    }
}

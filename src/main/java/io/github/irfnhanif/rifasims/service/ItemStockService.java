package io.github.irfnhanif.rifasims.service;

import io.github.irfnhanif.rifasims.dto.BarcodeScanResponse;
import io.github.irfnhanif.rifasims.dto.EditStockChangeRequest;
import io.github.irfnhanif.rifasims.dto.ScanStockChangeRequest;
import io.github.irfnhanif.rifasims.entity.*;
import io.github.irfnhanif.rifasims.exception.ResourceNotFoundException;
import io.github.irfnhanif.rifasims.repository.ItemStockRepository;
import io.github.irfnhanif.rifasims.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ItemStockService {

    private final ItemStockRepository itemStockRepository;
    private final UserRepository userRepository;
    private final StockAuditLogService stockAuditLogService;
    public ItemStockService(ItemStockRepository itemStockRepository, UserRepository userRepository, StockAuditLogService stockAuditLogService) {
        this.itemStockRepository = itemStockRepository;
        this.userRepository = userRepository;
        this.stockAuditLogService = stockAuditLogService;
    }

    public List<ItemStock> getAllItemStocks(String name, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page, size);
        if (name != null) {
            return itemStockRepository.findByItem_NameContaining(name, pageable).getContent();
        }
        return itemStockRepository.findAll(pageable).getContent();
    }

    public List<ItemStock> getItemStocksLessThanThreshold(Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page, size);
        return itemStockRepository.findItemStocksBelowThreshold(pageable).getContent();
    }

    public List<BarcodeScanResponse> getItemStocksByBarcode(String barcode) {
        List<BarcodeScanResponse> responses = itemStockRepository.findItemStocksByBarcode(barcode);
        return responses;
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
                getCurrentUser(),
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

        itemStock.setCurrentStock(newStock);
        itemStockRepository.save(itemStock);

        stockAuditLogService.recordStockChange(
                itemStock.getItem(),
                getCurrentUser(),
                scanStockChangeRequest.getChangeType(),
                oldStock,
                newStock,
                null,
                LocalDateTime.now()
        );

        return itemStock;
    }

    public void deleteItemStockChange(Item item) {
        ItemStock itemStock = itemStockRepository.findByItem(item).orElseThrow(() -> new ResourceNotFoundException("Item stock not found"));
        itemStockRepository.delete(itemStock);
    }

    public void restoreOldItemStock(Item item, StockChangeType stockChangeType, Integer oldStock) {
        ItemStock itemStock = itemStockRepository.findByItem(item).orElseThrow(() -> new ResourceNotFoundException("Item not found"));
        itemStock.setCurrentStock(oldStock);
        itemStockRepository.save(itemStock);
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}

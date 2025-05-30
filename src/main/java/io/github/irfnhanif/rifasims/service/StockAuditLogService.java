package io.github.irfnhanif.rifasims.service;

import io.github.irfnhanif.rifasims.entity.Item;
import io.github.irfnhanif.rifasims.entity.StockAuditLog;
import io.github.irfnhanif.rifasims.entity.StockChangeType;
import io.github.irfnhanif.rifasims.entity.User;
import io.github.irfnhanif.rifasims.exception.ResourceNotFoundException;
import io.github.irfnhanif.rifasims.repository.StockAuditLogRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class StockAuditLogService {

    private final StockAuditLogRepository stockAuditLogRepository;
    private final ItemStockService itemStockService;

    public StockAuditLogService(StockAuditLogRepository stockAuditLogRepository, @Lazy ItemStockService itemStockService) {
        this.stockAuditLogRepository = stockAuditLogRepository;
        this.itemStockService = itemStockService;
    }

    public List<StockAuditLog> getStockAuditLogs(String itemName, String userName, LocalDateTime fromDate, LocalDateTime toDate, Integer page, Integer size) {
        Specification<StockAuditLog> spec = Specification.where(null);
        if (itemName != null && !itemName.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("itemStock").get("item").get("name")),
                            "%" + itemName.toLowerCase() + "%"));
        }
        if (userName != null && !userName.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("itemStock").get("user").get("username")),
                            "%" + userName.toLowerCase() + "%"));
        }
        if (fromDate != null) {
            spec = spec.and((root, query, cb) ->
                    cb.greaterThanOrEqualTo(root.get("timestamp"), fromDate));
        }
        if (toDate != null) {
            spec = spec.and((root, query, cb) ->
                    cb.lessThanOrEqualTo(root.get("timestamp"), toDate));
        }

        return stockAuditLogRepository.findAll(spec, PageRequest.of(page, size)).getContent();
    }

    public List<StockAuditLog> getStockAuditLogsByItem(Item item, LocalDateTime fromDate, LocalDateTime toDate) {
        return stockAuditLogRepository.findAllByItemNameAndItemBarcodeAndTimestampBetween(item.getName(), item.getBarcode(), fromDate, toDate);
    }

    public StockAuditLog getStockAuditLogById(UUID stockAuditLogId) {
        return stockAuditLogRepository.findById(stockAuditLogId).orElseThrow(() -> new ResourceNotFoundException("Stock Audit Log Not Found"));
    }

    public StockAuditLog recordStockChange(Item item, User user, StockChangeType type, Integer oldStock, Integer newStock, String reason, LocalDateTime timestamp) {
        StockAuditLog stockAuditLog = new StockAuditLog();
        stockAuditLog.setItemName(item.getName());
        stockAuditLog.setItemBarcode(item.getBarcode());
        stockAuditLog.setUsername(user.getUsername());
        stockAuditLog.setType(type);
        stockAuditLog.setOldStock(oldStock);
        stockAuditLog.setNewStock(newStock);
        stockAuditLog.setReason(reason);
        stockAuditLog.setTimestamp(timestamp);
        return stockAuditLogRepository.save(stockAuditLog);
    }

    public void deleteStockAuditLog(UUID stockAuditLogId) {
        StockAuditLog stockAuditLog = stockAuditLogRepository.findById(stockAuditLogId).orElseThrow(() -> new ResourceNotFoundException("Stock Audit Log Not Found"));

        itemStockService.restoreOldItemStock(stockAuditLog.getItemName(), stockAuditLog.getType(), stockAuditLog.getOldStock());

        stockAuditLogRepository.delete(stockAuditLog);
    }
}

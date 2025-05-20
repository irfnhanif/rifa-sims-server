package io.github.irfnhanif.rifasims.service;

import io.github.irfnhanif.rifasims.entity.StockAuditLog;
import io.github.irfnhanif.rifasims.exception.ResourceNotFoundException;
import io.github.irfnhanif.rifasims.repository.StockAuditLogRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class StockAuditLogService {

    private final StockAuditLogRepository stockAuditLogRepository;

    public StockAuditLogService(StockAuditLogRepository stockAuditLogRepository) {
        this.stockAuditLogRepository = stockAuditLogRepository;
    }

    public List<StockAuditLog> getStockAuditLogs(String itemName, String userName, LocalDateTime fromDate, LocalDateTime toDate, Integer page, Integer size) {

    }

    public StockAuditLog getStockAuditLogById(UUID stockAuditLogId) {
        Optional<StockAuditLog> stockAuditLog = stockAuditLogRepository.findById(stockAuditLogId);
        if (!stockAuditLog.isPresent()) {
            throw new ResourceNotFoundException("Stock Audit Log Not Found");
        }
        return stockAuditLog.get();
    }

    public StockAuditLog recordStockChange(StockAuditLog stockAuditLog) {

    }

    public void deleteStockAuditLog(StockAuditLog stockAuditLog) {

    }
}

package io.github.irfnhanif.rifasims.service;

import io.github.irfnhanif.rifasims.entity.StockAuditLog;
import io.github.irfnhanif.rifasims.exception.ResourceNotFoundException;
import io.github.irfnhanif.rifasims.repository.StockAuditLogRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
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

    public StockAuditLog getStockAuditLogById(UUID stockAuditLogId) {
        Optional<StockAuditLog> stockAuditLog = stockAuditLogRepository.findById(stockAuditLogId);
        if (!stockAuditLog.isPresent()) {
            throw new ResourceNotFoundException("Stock Audit Log Not Found");
        }
        return stockAuditLog.get();
    }

    public StockAuditLog recordStockChange(StockAuditLog stockAuditLog) {
        return stockAuditLogRepository.save(stockAuditLog);
    }

    public void deleteStockAuditLog(UUID stockAuditLogId) {
        Optional<StockAuditLog> stockAuditLog = stockAuditLogRepository.findById(stockAuditLogId);
        if (!stockAuditLog.isPresent()) {
            throw new ResourceNotFoundException("Stock Audit Log Not Found");
        }
        stockAuditLogRepository.delete(stockAuditLog.get());
    }
}

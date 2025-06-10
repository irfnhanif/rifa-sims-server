package io.github.irfnhanif.rifasims.service;

import io.github.irfnhanif.rifasims.entity.Item;
import io.github.irfnhanif.rifasims.entity.StockAuditLog;
import io.github.irfnhanif.rifasims.entity.StockChangeType;
import io.github.irfnhanif.rifasims.entity.User;
import io.github.irfnhanif.rifasims.exception.ResourceNotFoundException;
import io.github.irfnhanif.rifasims.repository.StockAuditLogRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class StockAuditLogService {

    private final StockAuditLogRepository stockAuditLogRepository;
    private final ItemStockService itemStockService;
    private final ItemService itemService;
    private final UserService userService;

    public StockAuditLogService(StockAuditLogRepository stockAuditLogRepository, @Lazy ItemStockService itemStockService, @Lazy ItemService itemService, @Lazy UserService userService) {
        this.stockAuditLogRepository = stockAuditLogRepository;
        this.itemStockService = itemStockService;
        this.itemService = itemService;
        this.userService = userService;
    }

    public Map<String, Object> getStockAuditLogs(String itemName, String userName, List<StockChangeType> changeTypes, LocalDateTime fromDate, LocalDateTime toDate, Integer page, Integer size, String sortBy, String sortDirection, Boolean deleted) {
        Specification<StockAuditLog> spec = Specification.where(null);
        if (itemName != null && !itemName.isEmpty()) {
            try {
                Item item = itemService.getItemByName(itemName);
                spec = spec.and((root, query, cb) ->
                        cb.equal(root.get("itemId"), item.getId()));
            } catch (ResourceNotFoundException e) {
                spec = spec.and((root, query, cb) ->
                        cb.like(cb.lower(root.get("itemName")),
                                "%" + itemName.toLowerCase() + "%"));
            }
        }

        if (userName != null && !userName.isEmpty()) {
            try {
                User user = userService.getUserByUsername(userName);
                spec = spec.and((root, query, cb) ->
                        cb.equal(root.get("userId"), user.getId()));
            } catch (ResourceNotFoundException e) {
                spec = spec.and((root, query, cb) ->
                        cb.like(cb.lower(root.get("username")),
                                "%" + userName.toLowerCase() + "%"));
            }
        }
        if (changeTypes != null && !changeTypes.isEmpty()) {
            spec = spec.and((root, query, cb) -> root.get("type").in(changeTypes));
        }
        if (fromDate != null) {
            spec = spec.and((root, query, cb) ->
                    cb.greaterThanOrEqualTo(root.get("timestamp"), fromDate));
        }
        if (toDate != null) {
            spec = spec.and((root, query, cb) ->
                    cb.lessThanOrEqualTo(root.get("timestamp"), toDate));
        }
        if (deleted != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("deleted"), deleted));
        }

        PageRequest pageRequest;
        if (sortBy == null || sortBy.isEmpty()) {
            pageRequest = PageRequest.of(page, size);
        } else {
            String cleanDirection = sortDirection != null ? sortDirection.replace("\"", "") : "ASC";
            pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(cleanDirection), sortBy));
        }

        var pageResult = stockAuditLogRepository.findAll(spec, pageRequest);

        return Map.of(
                "logs", pageResult.getContent(),
                "totalCount", pageResult.getTotalElements(),
                "totalPages", pageResult.getTotalPages(),
                "currentPage", pageResult.getNumber()
        );
    }

    public List<StockAuditLog> getStockAuditLogsByItem(Item item, LocalDateTime fromDate, LocalDateTime toDate) {
        return stockAuditLogRepository.findAllByItemIdAndTimestampBetween(item.getId(), fromDate, toDate);
    }

    public List<StockAuditLog> getStockAuditLogsByItemName(String name) {
        return stockAuditLogRepository.findAllByItemName(name);
    }

    public List<StockAuditLog> getStockAuditLogsByItemBarcode(String barcode) {
        return stockAuditLogRepository.findAllByItemBarcode(barcode);
    }

    public List<StockAuditLog> getStockAuditLogsByUsername(String username) {
        return stockAuditLogRepository.findAllByUsername(username);
    }

    public StockAuditLog getStockAuditLogById(UUID stockAuditLogId) {
        return stockAuditLogRepository.findById(stockAuditLogId).orElseThrow(() -> new ResourceNotFoundException("Stock Audit Log Not Found"));
    }

    public StockAuditLog recordStockChange(Item item, User user, StockChangeType type, Integer oldStock, Integer newStock, String reason, LocalDateTime timestamp) {
        StockAuditLog stockAuditLog = new StockAuditLog();
        stockAuditLog.setItemId(item.getId());
        stockAuditLog.setItemName(item.getName());
        stockAuditLog.setItemBarcode(item.getBarcode());
        stockAuditLog.setUserId(user.getId());
        stockAuditLog.setUsername(user.getUsername());
        stockAuditLog.setType(type);
        stockAuditLog.setOldStock(oldStock);
        stockAuditLog.setNewStock(newStock);
        stockAuditLog.setReason(reason);
        stockAuditLog.setTimestamp(timestamp);
        stockAuditLog.setDeleted(false);
        stockAuditLog.setDeletedTimestamp(null);
        return stockAuditLogRepository.save(stockAuditLog);
    }

    public void saveStockAuditLogs(List<StockAuditLog> stockAuditLogs) {
        stockAuditLogRepository.saveAll(stockAuditLogs);
    }

    public void deleteStockAuditLog(UUID stockAuditLogId) {
        StockAuditLog stockAuditLog = stockAuditLogRepository.findById(stockAuditLogId)
                .orElseThrow(() -> new ResourceNotFoundException("Stock Audit Log Not Found"));

        // Soft delete the log instead of hard delete
        stockAuditLog.setDeleted(true);
        stockAuditLog.setDeletedTimestamp(LocalDateTime.now());
        stockAuditLogRepository.save(stockAuditLog);

        // Create a compensating transaction to maintain stock consistency
        createCompensatingTransaction(stockAuditLog);
    }

    private void createCompensatingTransaction(StockAuditLog deletedLog) {
        Integer difference = 0;
        if (deletedLog.getType() == StockChangeType.IN) {
            difference = deletedLog.getNewStock() - deletedLog.getOldStock();
        } else if (deletedLog.getType() == StockChangeType.OUT) {
            difference = deletedLog.getOldStock() - deletedLog.getNewStock();
        }

        // Call the existing method to update the physical stock value
        Map<String, Integer> returnedStockValues = itemStockService.restoreOldItemStock(deletedLog.getItemName(), deletedLog.getType(), difference);

        // Get current user and item for creating a compensation log
        User currentUser = userService.getCurrentUser();
        Item item = itemService.getItemByName(deletedLog.getItemName());

        User system = new User();
        system.setUsername("System");

        // Record the compensation in the audit trail
        recordStockChange(
                item,
                system,
                StockChangeType.AUTO_EDIT,
                returnedStockValues.get("oldStock"),
                returnedStockValues.get("newStock"),
                "Kompensasi audit log: " + deletedLog.getId() + " yang dihapus " + currentUser.getUsername(),
                LocalDateTime.now()
        );
    }
}

package io.github.irfnhanif.rifasims.controller;

import io.github.irfnhanif.rifasims.dto.APIResponse;
import io.github.irfnhanif.rifasims.entity.StockAuditLog;
import io.github.irfnhanif.rifasims.exception.InternalServerException;
import io.github.irfnhanif.rifasims.service.StockAuditLogService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/logs")
public class StockAuditLogController {

    private final StockAuditLogService stockAuditLogService;

    public StockAuditLogController(StockAuditLogService stockAuditLogService) {
        this.stockAuditLogService = stockAuditLogService;
    }

    @GetMapping("")
    public ResponseEntity<APIResponse<List<StockAuditLog>>> getStockAuditLogs(@RequestParam(required = false) String itemName,
                                                              @RequestParam(required = false) String username,
                                                              @RequestParam(required = false) LocalDateTime fromDate,
                                                              @RequestParam(required = false) LocalDateTime toDate,
                                                              @RequestParam(required = false, defaultValue = "0") Integer page,
                                                              @RequestParam(required = false, defaultValue = "10") Integer size) {
        try {
            List<StockAuditLog> logs = stockAuditLogService.getStockAuditLogs(itemName, username, fromDate, toDate, page, size);
            return ResponseEntity.ok(new APIResponse<>(true, "Stock audit log retrieved successfully", logs, null));
        } catch (Exception e) {
            throw new InternalServerException(e.getMessage());
        }
    }

    @DeleteMapping("/{logId}")
    public ResponseEntity<APIResponse<Void>> deleteStockAuditLog(@PathVariable UUID logId) {
        try {
            stockAuditLogService.deleteStockAuditLog(logId);
            return ResponseEntity.ok(new APIResponse<>(true, "Stock audit log deleted successfully", null, null));
        } catch (Exception e) {
            throw new InternalServerException(e.getMessage());
        }
    }
}

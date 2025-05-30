package io.github.irfnhanif.rifasims.repository;

import io.github.irfnhanif.rifasims.entity.Item;
import io.github.irfnhanif.rifasims.entity.StockAuditLog;
import io.github.irfnhanif.rifasims.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface StockAuditLogRepository  extends JpaRepository<StockAuditLog, UUID>, JpaSpecificationExecutor<StockAuditLog> {
    List<StockAuditLog> findAllByItemNameAndItemBarcodeAndTimestampBetween(String name, String barcode, LocalDateTime fromDate, LocalDateTime toDate);
}

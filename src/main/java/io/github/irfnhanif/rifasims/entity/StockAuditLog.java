package io.github.irfnhanif.rifasims.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(indexes = {
        @Index(name = "idx_stock_audit_log_timestamp", columnList = "timestamp"),
        @Index(name = "idx_stock_audit_log_composite_item_id_timestamp", columnList = "item_id, timestamp")
})
public class StockAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String itemName;

    @Column(nullable = false)
    private String itemBarcode;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private StockChangeType type;

    @Column(nullable = false)
    private Integer oldStock;

    @Column(nullable = false)
    private Integer newStock;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String reason;

    // Store all timestamps in Jakarta/Indonesia timezone (GMT+7)
    private LocalDateTime timestamp;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getItemBarcode() {
        return itemBarcode;
    }

    public void setItemBarcode(String itemBarcode) {
        this.itemBarcode = itemBarcode;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public StockChangeType getType() {
        return type;
    }

    public void setType(StockChangeType type) {
        this.type = type;
    }

    public Integer getOldStock() {
        return oldStock;
    }

    public void setOldStock(Integer oldStock) {
        this.oldStock = oldStock;
    }

    public Integer getNewStock() {
        return newStock;
    }

    public void setNewStock(Integer newStock) {
        this.newStock = newStock;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public LocalDateTime getTimestamp() {return timestamp;}

    public void setTimestamp(LocalDateTime timestamp) {this.timestamp = timestamp;}
}

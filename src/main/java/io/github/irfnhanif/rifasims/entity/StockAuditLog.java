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
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private StockChangeType type;

    @Column(nullable = false)
    private int oldStock;

    @Column(nullable = false)
    private int newStock;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String reason;

    private LocalDateTime timestamp;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public StockChangeType getType() {
        return type;
    }

    public void setType(StockChangeType type) {
        this.type = type;
    }

    public int getOldStock() {
        return oldStock;
    }

    public void setOldStock(int oldStock) {
        this.oldStock = oldStock;
    }

    public int getNewStock() {
        return newStock;
    }

    public void setNewStock(int newStock) {
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

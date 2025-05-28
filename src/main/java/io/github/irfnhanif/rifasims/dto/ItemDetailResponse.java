package io.github.irfnhanif.rifasims.dto;

import io.github.irfnhanif.rifasims.entity.Item;
import io.github.irfnhanif.rifasims.entity.StockAuditLog;

import java.util.List;

public class ItemDetailResponse {
    private Item item;
    private List<StockAuditLog> auditLogs;

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public List<StockAuditLog> getAuditLogs() {
        return auditLogs;
    }

    public void setAuditLogs(List<StockAuditLog> auditLogs) {
        this.auditLogs = auditLogs;
    }
}

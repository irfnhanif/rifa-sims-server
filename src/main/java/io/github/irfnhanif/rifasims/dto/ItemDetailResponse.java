package io.github.irfnhanif.rifasims.dto;

import io.github.irfnhanif.rifasims.entity.Item;
import io.github.irfnhanif.rifasims.entity.StockAuditLog;

import java.util.List;

public class ItemDetailResponse {
    private Item item;
    private List<StockAuditLog> stockAuditLogs;

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public List<StockAuditLog> getStockAuditLogs() {
        return stockAuditLogs;
    }

    public void setStockAuditLogs(List<StockAuditLog> stockAuditLogs) {
        this.stockAuditLogs = stockAuditLogs;
    }
}

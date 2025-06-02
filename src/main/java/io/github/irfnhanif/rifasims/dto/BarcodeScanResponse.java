package io.github.irfnhanif.rifasims.dto;

import java.util.UUID;

public class BarcodeScanResponse {
    private UUID itemStockId;
    private String itemName;
    private Integer currentStock;

    public BarcodeScanResponse(UUID itemStockId, String itemName, Integer currentStock) {
        this.itemStockId = itemStockId;
        this.itemName = itemName;
        this.currentStock = currentStock;
    }

    public UUID getItemStockId() {
        return itemStockId;
    }

    public void setItemStockId(UUID itemStockId) {
        this.itemStockId = itemStockId;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public Integer getCurrentStock() {
        return currentStock;
    }

    public void setCurrentStock(Integer currentStock) {
        this.currentStock = currentStock;
    }
}

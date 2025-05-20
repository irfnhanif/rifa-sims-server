package io.github.irfnhanif.rifasims.dto;

import java.util.UUID;

public class BarcodeScanResponse {
    private UUID itemStockId;
    private String itemName;

    public BarcodeScanResponse(UUID itemStockId, String itemName) {
        this.itemStockId = itemStockId;
        this.itemName = itemName;
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
}

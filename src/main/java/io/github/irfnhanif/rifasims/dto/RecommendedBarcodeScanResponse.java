package io.github.irfnhanif.rifasims.dto;

import java.util.UUID;

public class RecommendedBarcodeScanResponse {
    UUID itemStockId;
    String itemName;
    Integer currentStock;
    String recommendationScore;

    public RecommendedBarcodeScanResponse(UUID itemStockId, String itemName, Integer currentStock, String recommendationScore) {
        this.itemStockId = itemStockId;
        this.itemName = itemName;
        this.currentStock = currentStock;
        this.recommendationScore = recommendationScore;
    }

    public UUID getItemStockId() {
        return itemStockId;
    }

    public String getItemName() {
        return itemName;
    }

    public Integer getCurrentStock() {
        return currentStock;
    }

    public String getRecommendationScore() {
        return recommendationScore;
    }
}

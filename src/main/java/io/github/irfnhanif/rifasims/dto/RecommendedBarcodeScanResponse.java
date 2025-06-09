package io.github.irfnhanif.rifasims.dto;

public class RecommendedBarcodeScanResponse {
    String itemStockId;
    String itemName;
    Integer currentStock;
    String recommendationScore;

    public RecommendedBarcodeScanResponse(String itemStockId, String itemName, Integer currentStock, String recommendationScore) {
        this.itemStockId = itemStockId;
        this.itemName = itemName;
        this.currentStock = currentStock;
        this.recommendationScore = recommendationScore;
    }

    public String getItemStockId() {
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

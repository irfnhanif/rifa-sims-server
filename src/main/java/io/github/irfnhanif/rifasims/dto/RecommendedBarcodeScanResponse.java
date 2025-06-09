package io.github.irfnhanif.rifasims.dto;

public class RecommendedBarcodeScanResponse {
    String itemStockId;
    String itemName;
    String recommendationScore;

    public RecommendedBarcodeScanResponse(String itemStockId, String itemName, String recommendationScore) {
        this.itemStockId = itemStockId;
        this.itemName = itemName;
        this.recommendationScore = recommendationScore;
    }

    public String getItemStockId() {
        return itemStockId;
    }

    public String getItemName() {
        return itemName;
    }

    public String getRecommendationScore() {
        return recommendationScore;
    }
}

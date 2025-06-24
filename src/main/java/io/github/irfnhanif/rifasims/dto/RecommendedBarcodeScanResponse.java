package io.github.irfnhanif.rifasims.dto;

import java.util.UUID;

public class RecommendedBarcodeScanResponse {
    private UUID itemStockId;
    private String itemName;
    private Integer currentStock;
    private Long wholesalePrice;
    private String recommendationScore;

    public RecommendedBarcodeScanResponse(UUID itemStockId, String itemName, Integer currentStock, Long wholesalePrice, String recommendationScore) {
        this.itemStockId = itemStockId;
        this.itemName = itemName;
        this.currentStock = currentStock;
        this.wholesalePrice = wholesalePrice;
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

    public Long getWholesalePrice() {
        return wholesalePrice;
    }
}

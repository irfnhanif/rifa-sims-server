package io.github.irfnhanif.rifasims.util;

import io.github.irfnhanif.rifasims.dto.RecommendedBarcodeScanResponse;
import io.github.irfnhanif.rifasims.entity.Item;
import io.github.irfnhanif.rifasims.entity.ItemStock;
import io.github.irfnhanif.rifasims.entity.StockAuditLog;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ScanItemRecommender {
    private static final double WEIGHT_FREQUENCY = 0.6;
    private static final double WEIGHT_RECENCY = 0.4;
    private static final long MAX_TIME_ELAPSED_SECONDS = 30L * 24L * 60L * 60L;

    // Private helper class to hold raw data before scoring.
    private static class ItemStockRawData {
        final ItemStock itemStock;
        final long rawFrequency;
        final LocalDateTime rawTime;

        ItemStockRawData(ItemStock itemStock, long rawFrequency, LocalDateTime rawTime) {
            this.itemStock = itemStock;
            this.rawFrequency = rawFrequency;
            this.rawTime = rawTime;
        }
    }

    // Private helper class to hold intermediate data and the final score.
    private static class RankedItemStock {
        final ItemStock itemStock;
        final double score;

        RankedItemStock(ItemStock itemStock, double score) {
            this.itemStock = itemStock;
            this.score = score;
        }
    }

    public List<RecommendedBarcodeScanResponse> recommendItem(
            List<ItemStock> matchingItemStocks,
            List<StockAuditLog> stockAuditLogCollection) {

        if (matchingItemStocks == null || matchingItemStocks.isEmpty()) {
            return Collections.emptyList();
        }

        // If only one item, return it with a perfect score.
        if (matchingItemStocks.size() == 1) {
            ItemStock singleItemStock = matchingItemStocks.getFirst();
            return Collections.singletonList(new RecommendedBarcodeScanResponse(
                    singleItemStock.getId().toString(),
                    singleItemStock.getItem().getName(),
                    singleItemStock.getCurrentStock(),
                    "1.00"
            ));
        }

        // Step 1: Gather raw frequency and recency data for each item
        List<RankedItemStock> scoredItems = new ArrayList<>();

        long maxRawFrequency = 0;
        // This temporary list holds raw data before normalization
        List<ItemStockRawData> temporaryList = new ArrayList<>();

        for (ItemStock itemStock : matchingItemStocks) {
            Item item = itemStock.getItem();
            List<StockAuditLog> itemHistoryEntries = stockAuditLogCollection.stream()
                    .filter(log -> log.getItemBarcode().equals(item.getBarcode()) && log.getItemName().equals(item.getName()))
                    .toList();

            long frequency = itemHistoryEntries.size();
            if (frequency > maxRawFrequency) {
                maxRawFrequency = frequency;
            }

            LocalDateTime lastScannedTime = itemHistoryEntries.stream()
                    .map(StockAuditLog::getTimestamp)
                    .max(LocalDateTime::compareTo)
                    .orElse(LocalDateTime.of(1970, 1, 1, 0, 0)); // A very old datetime

            temporaryList.add(new ItemStockRawData(itemStock, frequency, lastScannedTime));
        }

        LocalDateTime currentTime = LocalDateTime.now();

        // Step 2: Calculate normalized scores and create a list of ranked items
        for (ItemStockRawData rawData : temporaryList) {
            double normalizedFrequencyScore = (maxRawFrequency > 0)
                    ? (double) rawData.rawFrequency / maxRawFrequency : 0;

            long timeElapsedSeconds = Duration.between(rawData.rawTime, currentTime).getSeconds();
            double normalizedRecencyScore = 0;
            if (timeElapsedSeconds < MAX_TIME_ELAPSED_SECONDS) {
                normalizedRecencyScore = 1.0 - ((double) timeElapsedSeconds / MAX_TIME_ELAPSED_SECONDS);
            }

            double recommendationScore = (WEIGHT_FREQUENCY * normalizedFrequencyScore) + (WEIGHT_RECENCY * normalizedRecencyScore);

            scoredItems.add(new RankedItemStock(rawData.itemStock, recommendationScore));
        }

        // Step 3: Sort the list by score in descending order
        scoredItems.sort(Comparator.comparingDouble((RankedItemStock item) -> item.score).reversed());

        // Step 4: Map the sorted list to the final DTO response
        return scoredItems.stream()
                .map(rankedItem -> new RecommendedBarcodeScanResponse(
                        rankedItem.itemStock.getId().toString(),
                        rankedItem.itemStock.getItem().getName(),
                        rankedItem.itemStock.getCurrentStock(),
                        String.format("%.2f", rankedItem.score) // Format score to 2 decimal places
                ))
                .collect(Collectors.toList());
    }
}

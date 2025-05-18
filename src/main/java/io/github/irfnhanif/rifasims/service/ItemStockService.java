package io.github.irfnhanif.rifasims.service;

import io.github.irfnhanif.rifasims.dto.StockScanRequest;
import io.github.irfnhanif.rifasims.entity.ItemStock;
import io.github.irfnhanif.rifasims.repository.ItemStockRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ItemStockService {

    private final ItemStockRepository itemStockRepository;
    public ItemStockService(ItemStockRepository itemStockRepository) {
        this.itemStockRepository = itemStockRepository;
    }

    public List<ItemStock> getAllItemStocks(String name, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page, size);
        if (name != null) {
            return itemStockRepository.findByItem_NameContaining(name, pageable).getContent();
        }
        return itemStockRepository.findAll(pageable).getContent();
    }

    public List<ItemStock> getItemStocksLessThanThreshold() {

    }

    public ItemStock getItemStockById(UUID itemStockId) {

    }

    public ItemStock saveItemStockChange(ItemStock itemStock) {

    }

    public ItemStock updateItemStockChange(UUID itemStockId, ItemStock itemStock) {

    }

    public ItemStock updateScanItemStockChange(UUID itemStockId, StockScanRequest stockScanRequest) {

    }

    public void deleteItemStockChange(ItemStock itemStock) {

    }
}

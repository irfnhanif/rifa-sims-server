package io.github.irfnhanif.rifasims.service;

import io.github.irfnhanif.rifasims.dto.StockChangeRequest;
import io.github.irfnhanif.rifasims.entity.ItemStock;
import io.github.irfnhanif.rifasims.entity.StockChangeType;
import io.github.irfnhanif.rifasims.exception.ResourceNotFoundException;
import io.github.irfnhanif.rifasims.repository.ItemStockRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
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

    public List<ItemStock> getItemStocksLessThanThreshold(Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page, size);
        return itemStockRepository.findItemStocksBelowThreshold(pageable).getContent();
    }

    public ItemStock getItemStockById(UUID itemStockId) {
        Optional<ItemStock> itemStock = itemStockRepository.findById(itemStockId);
        if (!itemStock.isPresent()) {
            throw new ResourceNotFoundException("Item stock not found");
        }
        return itemStock.get();
    }

    public ItemStock createItemStock(ItemStock itemStock) {
        return itemStockRepository.save(itemStock);
    }


    public ItemStock updateItemStockChange(UUID itemStockId, ItemStock itemStock) {
        Optional<ItemStock> itemStockOptional = itemStockRepository.findById(itemStockId);
        if (!itemStockOptional.isPresent()) {
            throw new ResourceNotFoundException("Item stock not found");
        }
        itemStock.setId(itemStockId);
        itemStockRepository.save(itemStock);
        return itemStock;
    }

    public ItemStock updateScanItemStockChange(UUID itemStockId, StockChangeRequest stockChangeRequest) {
        Optional<ItemStock> itemStockOptional = itemStockRepository.findById(itemStockId);
        if (!itemStockOptional.isPresent()) {
            throw new ResourceNotFoundException("Item stock not found");
        }
        ItemStock itemStock = itemStockOptional.get();
        if (stockChangeRequest.getChangeType() == StockChangeType.OUT) {
            itemStock.setCurrentStock(itemStock.getCurrentStock() - stockChangeRequest.getAmount());
        } else {
            itemStock.setCurrentStock(itemStock.getCurrentStock() + stockChangeRequest.getAmount());
        }
        itemStockRepository.save(itemStock);
        return itemStock;
    }

    public void deleteItemStockChange(UUID itemStockId) {
        Optional<ItemStock> itemStockOptional = itemStockRepository.findById(itemStockId);
        if (!itemStockOptional.isPresent()) {
            throw new ResourceNotFoundException("Item stock not found");
        }
        itemStockRepository.delete(itemStockOptional.get());
    }
}

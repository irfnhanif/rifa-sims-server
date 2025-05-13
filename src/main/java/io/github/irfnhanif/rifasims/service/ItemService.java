package io.github.irfnhanif.rifasims.service;

import io.github.irfnhanif.rifasims.entity.Item;
import io.github.irfnhanif.rifasims.entity.StockAuditLog;
import io.github.irfnhanif.rifasims.exception.ResourceNotFoundException;
import io.github.irfnhanif.rifasims.repository.ItemRepository;
import io.github.irfnhanif.rifasims.repository.StockAuditLogRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ItemService {

    private ItemRepository itemRepository;
    private StockAuditLogRepository stockAuditLogRepository;

    public ItemService(ItemRepository itemRepository, StockAuditLogRepository stockAuditLogRepository) {
        this.itemRepository = itemRepository;
        this.stockAuditLogRepository = stockAuditLogRepository;
    }

    public List<Item> getAllItems(String name, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page, size);
        if (name != null) {
            return itemRepository.findByNameContaining(name, pageable).getContent();
        }
        return itemRepository.findAll(pageable).getContent();
    }

    public List<Item> getItemsByBarcode(String barcode) {
        return itemRepository.findByBarcode(barcode);
    }

    public List<StockAuditLog> getStockAuditLogsByItemId(UUID itemId) {
        Optional<Item> item = itemRepository.findById(itemId);
        if (!item.isPresent()) {
            return new ArrayList<>();
        }
        return stockAuditLogRepository.findAllByItem(item.get());
    }

    public Item getItemById(UUID id) {
        Optional<Item> item = itemRepository.findById(id);
        if (!item.isPresent()) {
            throw new ResourceNotFoundException("Item not found");
        }
        return item.get();
    }


    public Item createItem(Item item) {
        item.setId(UUID.randomUUID());
        return itemRepository.save(item);
    }

    public Item updateItem(Item item) {
        Optional<Item> itemOptional = itemRepository.findById(item.getId());
        if (!itemOptional.isPresent()) {
            throw new ResourceNotFoundException("Item not found");
        }
        item.setId(item.getId());
        itemRepository.save(item);
        return item;
    }

    public void deleteItem(UUID id) {
        Optional<Item> itemOptional = itemRepository.findById(id);
        if (!itemOptional.isPresent()) {
            throw new ResourceNotFoundException("Item not found");
        }
        itemRepository.delete(itemOptional.get());
    }
}

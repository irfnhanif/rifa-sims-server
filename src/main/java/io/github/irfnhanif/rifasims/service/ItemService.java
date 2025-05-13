package io.github.irfnhanif.rifasims.service;

import io.github.irfnhanif.rifasims.entity.Item;
import io.github.irfnhanif.rifasims.entity.StockAuditLog;
import io.github.irfnhanif.rifasims.repository.ItemRepository;
import io.github.irfnhanif.rifasims.repository.StockAuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ItemService {

    private ItemRepository itemRepository;
    private StockAuditLogRepository stockAuditLogRepository;

    public ItemService(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    public List<Item> getAllItems() {
        return itemRepository.findAll();
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
        return itemRepository.findById(id).orElse(null);
    }


    public Item createItem(Item item) {
        item.setId(UUID.randomUUID());
        return itemRepository.save(item);
    }

    public Item updateItem(Item item) {
        Optional<Item> itemOptional = itemRepository.findById(item.getId());
        if (!itemOptional.isPresent()) {
            return null;
        }
        item.setId(item.getId());
        return itemRepository.save(item);
    }

    public void deleteItem(UUID id) {
        Optional<Item> itemOptional = itemRepository.findById(id);
        if (!itemOptional.isPresent()) {
            return;
        }
        itemRepository.delete(itemOptional.get());
    }
}

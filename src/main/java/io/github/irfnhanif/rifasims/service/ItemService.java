package io.github.irfnhanif.rifasims.service;

import io.github.irfnhanif.rifasims.dto.BarcodeScanResponse;
import io.github.irfnhanif.rifasims.dto.CreateItemRequest;
import io.github.irfnhanif.rifasims.dto.ItemDetailResponse;
import io.github.irfnhanif.rifasims.entity.*;
import io.github.irfnhanif.rifasims.exception.ResourceNotFoundException;
import io.github.irfnhanif.rifasims.repository.ItemRepository;
import io.github.irfnhanif.rifasims.repository.ItemStockRepository;
import io.github.irfnhanif.rifasims.repository.StockAuditLogRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ItemService {

    private final ItemStockRepository itemStockRepository;
    private ItemRepository itemRepository;
    private StockAuditLogService stockAuditLogService;
    private ItemStockService itemStockService;


    public ItemService(ItemRepository itemRepository, StockAuditLogService stockAuditLogService, ItemStockService itemStockService, ItemStockRepository itemStockRepository) {
        this.itemRepository = itemRepository;
        this.stockAuditLogService = stockAuditLogService;
        this.itemStockService = itemStockService;
        this.itemStockRepository = itemStockRepository;
    }

    public List<Item> getAllItems(String name, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page, size);
        if (name != null) {
            return itemRepository.findByNameContaining(name, pageable).getContent();
        }
        return itemRepository.findAll(pageable).getContent();
    }

    public List<BarcodeScanResponse> getItemsByBarcode(String barcode) {
        List<BarcodeScanResponse> responses = itemStockRepository.findItemStocksByBarcode(barcode);
        return responses;
    }

    public ItemDetailResponse getItemById(UUID itemId, LocalDateTime fromDate, LocalDateTime toDate) {
        Item item = itemRepository.findById(itemId).orElseThrow(() -> new ResourceNotFoundException("Item not found"));

        List<StockAuditLog> logs = stockAuditLogService.getStockAuditLogsByItem(item,fromDate,toDate);

        ItemDetailResponse response = new ItemDetailResponse();
        response.setItem(item);
        response.setAuditLogs(logs);

        return itemDetailResponse;
    }

    public Item createItem(CreateItemRequest createItemRequest) {
        Item item = createNewItem(createItemRequest.getName(), createItemRequest.getBarcode(), createItemRequest.getDescription());
        Item savedItem = itemRepository.save(item);

        ItemStock itemStock = createNewItemStock(savedItem, createItemRequest.getCurrentStock(), createItemRequest.getThreshold());
        itemStockService.createItemStock(itemStock);

        return savedItem;
    }

    public Item updateItem(UUID itemId, Item item) {
        if (!itemRepository.existsById(itemId)) {
            throw new ResourceNotFoundException("Item not found");
        }
        item.setId(itemId);
        return itemRepository.save(item);
    }

    public void deleteItem(UUID id) {
        Item item = itemRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Item not found"));
        itemRepository.delete(item);
    }

    private Item createNewItem(String itemName, String itemBarcode, String itemDescription) {
        Item item = new Item();
        item.setName(itemName);
        item.setBarcode(itemBarcode);
        item.setDescription(itemDescription);
        return item;
    }

    private ItemStock createNewItemStock(Item item, Integer currentStock, Integer threshold) {
        ItemStock itemStock = new ItemStock();
        itemStock.setItem(item);
        itemStock.setCurrentStock(currentStock);
        itemStock.setThreshold(threshold);
        return itemStock;
    }
}

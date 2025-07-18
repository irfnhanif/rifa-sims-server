package io.github.irfnhanif.rifasims.service;

import io.github.irfnhanif.rifasims.dto.CreateItemRequest;
import io.github.irfnhanif.rifasims.dto.ItemDetailResponse;
import io.github.irfnhanif.rifasims.entity.*;
import io.github.irfnhanif.rifasims.exception.ResourceNotFoundException;
import io.github.irfnhanif.rifasims.repository.ItemRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ItemService {

    private ItemRepository itemRepository;
    private StockAuditLogService stockAuditLogService;
    private ItemStockService itemStockService;
    private UserService userService;


    public ItemService(ItemRepository itemRepository, StockAuditLogService stockAuditLogService, ItemStockService itemStockService, UserService userService) {
        this.itemRepository = itemRepository;
        this.stockAuditLogService = stockAuditLogService;
        this.itemStockService = itemStockService;
        this.userService = userService;
    }

    public List<Item> getAllItems(String name, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page, size);
        if (name != null) {
            return itemRepository.findByNameContainingIgnoreCaseAndDeletedFalse(name, pageable).getContent();
        }
        return itemRepository.findByDeletedFalse(pageable).getContent();
    }

    public Item getItemById(UUID id) {
        return itemRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found"));
    }

    public ItemDetailResponse getItemById(UUID itemId, LocalDateTime fromDate, LocalDateTime toDate) {
        Item item = itemRepository.findById(itemId).orElseThrow(() -> new ResourceNotFoundException("Item not found"));

        List<StockAuditLog> logs = stockAuditLogService.getStockAuditLogsByItem(item,fromDate,toDate);

        ItemDetailResponse response = new ItemDetailResponse();
        response.setItem(item);
        response.setStockAuditLogs(logs);

        return response;
    }

    public Item getItemByName(String name) {
        return itemRepository.findByNameIgnoreCase(name).orElseThrow(() -> new ResourceNotFoundException("Item not found"));
    }

    @Transactional
    public Item createItem(CreateItemRequest createItemRequest) {
        Item item = createNewItem(
                createItemRequest.getName(),
                createItemRequest.getBarcode(),
                createItemRequest.getDescription(),
                createItemRequest.getWholesalePrice(),
                createItemRequest.getProfitPercentage(),
                createItemRequest.getRetailPrice()
        );
        Item savedItem = itemRepository.save(item);

        ItemStock itemStock = createNewItemStock(savedItem, createItemRequest.getCurrentStock(), createItemRequest.getThreshold());
        itemStockService.createItemStock(itemStock);

        stockAuditLogService.recordStockChange(
                savedItem,
                userService.getCurrentUser(),
                StockChangeType.CREATE,
                0,
                createItemRequest.getCurrentStock(),
                null,
                LocalDateTime.now()
        );

        return savedItem;
    }

    public Item updateItem(UUID itemId, Item item) {
       if (!itemRepository.findById(itemId).isPresent()) {
           throw new ResourceNotFoundException("Item not found");
       }

        item.setId(itemId);
        return itemRepository.save(item);
    }

    @Transactional
    public void deleteItem(UUID id) {
        Item item = itemRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Item not found"));

        Integer oldStock = itemStockService.deleteItemStockChange(item);

        item.setDeleted(true);
        itemRepository.save(item);

        stockAuditLogService.recordStockChange(
                item,
                userService.getCurrentUser(),
                StockChangeType.DELETE,
                oldStock,
                0,
                null,
                LocalDateTime.now()
        );
    }

    private Item createNewItem(String itemName, String itemBarcode, String itemDescription, Long itemWholesalePrice, Double itemProfitPercentage, Long itemRetailPrice) {
        Item item = new Item();
        item.setName(itemName);
        item.setBarcode(itemBarcode);
        item.setDescription(itemDescription);
        item.setWholesalePrice(itemWholesalePrice);
        item.setProfitPercentage(itemProfitPercentage);
        item.setRetailPrice(itemRetailPrice);
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

package io.github.irfnhanif.rifasims.service;

import io.github.irfnhanif.rifasims.entity.ItemStock;
import io.github.irfnhanif.rifasims.entity.NotificationType;
import io.github.irfnhanif.rifasims.entity.SystemNotification;
import io.github.irfnhanif.rifasims.entity.User;
import io.github.irfnhanif.rifasims.exception.ResourceNotFoundException;
import io.github.irfnhanif.rifasims.repository.SystemNotificationRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class SystemNotificationService {

    private final ItemStockService itemStockService;
    private final UserService userService;
    private final SystemNotificationRepository systemNotificationRepository;

    public SystemNotificationService(ItemStockService itemStockService, UserService userService, SystemNotificationRepository systemNotificationRepository) {
        this.itemStockService = itemStockService;
        this.userService = userService;
        this.systemNotificationRepository = systemNotificationRepository;
    }

    public List<SystemNotification> getNotificationsForOwner() {
        return systemNotificationRepository.findByReadFalseOrderByCreatedAtDesc();
    }

    public void markAsRead(UUID notificationId) {
        SystemNotification notification = systemNotificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
        notification.setRead(true);
        systemNotificationRepository.save(notification);
    }

    public void markAllAsRead() {
        List<SystemNotification> unreadNotifications = systemNotificationRepository.findByReadFalse();
        unreadNotifications.forEach(n -> n.setRead(true));
        systemNotificationRepository.saveAll(unreadNotifications);
    }


    @Scheduled(fixedRate = 60000)
    public void checkForNotificationEvents() {
        checkLowStockItems();
        checkPendingUsers();
    }

    private void checkLowStockItems() {
        List<ItemStock> lowStockItems = itemStockService.getAllItemStocksBelowThreshold();

        for (ItemStock stock : lowStockItems) {
            if (!systemNotificationRepository.existsByTypeAndReferenceIdAndReadFalseAndCreatedAtAfter(
                    NotificationType.LOW_STOCK, stock.getId(), LocalDateTime.now().minusDays(1))) {

                createNotification(
                        NotificationType.LOW_STOCK,
                        stock.getId(),
                        "Peringatan Stok Barang: \n" + stock.getItem().getName(),
                        "Jumlah barang " + stock.getItem().getName() + " di bawah batas minimal. \nJumlah sekarang: " + stock.getCurrentStock() + ", Batas minimal: " + stock.getThreshold()
                );
            }
        }
    }

    private void checkPendingUsers() {
        List<User> pendingUsers = userService.getPendingUsersAndAddedToNotificationFalse();

        for (User user : pendingUsers) {
            createNotification(
                    NotificationType.NEW_USER,
                    user.getId(),
                    "Registrasi Pengguna Baru",
                    "Pengguna " + user.getUsername() + " telah registrasi dan sedang menunggu persetujuan"
            );

            userService.setUserAddedToNotificationTrue(user.getId());
        }
    }

    private void createNotification(NotificationType type, UUID referenceId, String title, String message) {
        SystemNotification notification = new SystemNotification();
        notification.setType(type);
        notification.setReferenceId(referenceId);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setRead(false);

        systemNotificationRepository.save(notification);
    }
}

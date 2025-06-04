package io.github.irfnhanif.rifasims.controller;

import io.github.irfnhanif.rifasims.dto.APIResponse;
import io.github.irfnhanif.rifasims.entity.SystemNotification;
import io.github.irfnhanif.rifasims.service.SystemNotificationService;
import jakarta.ws.rs.InternalServerErrorException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
public class SystemNotificationController {

    private final SystemNotificationService systemNotificationService;

    public SystemNotificationController(SystemNotificationService systemNotificationService) {
        this.systemNotificationService = systemNotificationService;
    }

    @GetMapping("")
    public ResponseEntity<APIResponse<List<SystemNotification>>> getNotifications() {
        try {
            List<SystemNotification> notifications = systemNotificationService.getNotificationsForOwner();
            return ResponseEntity.ok(new APIResponse<>(true, "Berhasil mengambil data notifikasi", notifications, null));
        } catch (Exception e) {
            throw new InternalServerErrorException(e.getMessage());
        }
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<APIResponse<Void>> markAsRead(@PathVariable UUID id) {
        try {
            systemNotificationService.markAsRead(id);
            return ResponseEntity.ok(new APIResponse<>(true, "Notifikasi berhasil ditandai", null, null));
        } catch (Exception e) {
            throw new InternalServerErrorException(e.getMessage());
        }
    }

    @PutMapping("/read-all")
    public ResponseEntity<APIResponse<Void>> markAllAsRead() {
        try {
            systemNotificationService.markAllAsRead();
            return ResponseEntity.ok(new APIResponse<>(true, "Seluruh notifikasi berhasil ditandai", null, null));
        } catch (Exception e) {
            throw new InternalServerErrorException(e.getMessage());
        }
    }
}

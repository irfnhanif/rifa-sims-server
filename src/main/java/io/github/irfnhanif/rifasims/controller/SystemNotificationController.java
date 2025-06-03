package io.github.irfnhanif.rifasims.controller;

import io.github.irfnhanif.rifasims.entity.SystemNotification;
import io.github.irfnhanif.rifasims.service.SystemNotificationService;
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

    @GetMapping
    public ResponseEntity<List<SystemNotification>> getNotifications() {
        return ResponseEntity.ok(systemNotificationService.getNotificationsForOwner());
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable UUID id) {
        systemNotificationService.markAsRead(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead() {
        systemNotificationService.markAllAsRead();
        return ResponseEntity.ok().build();
    }
}

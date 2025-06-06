package io.github.irfnhanif.rifasims.service;

import io.github.irfnhanif.rifasims.entity.StockAuditLog;
import io.github.irfnhanif.rifasims.entity.User;
import io.github.irfnhanif.rifasims.entity.UserStatus;
import io.github.irfnhanif.rifasims.exception.ResourceNotFoundException;
import io.github.irfnhanif.rifasims.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final StockAuditLogService stockAuditLogService;

    public UserService(UserRepository userRepository,  StockAuditLogService stockAuditLogService) {
        this.userRepository = userRepository;
        this.stockAuditLogService = stockAuditLogService;
    }

    public List<User> getPendingUsers() {
        List<User> pendingUsers = userRepository.findByStatus(UserStatus.PENDING);
        return pendingUsers;
    }

    public List<User> getPendingUsersAndAddedToNotificationFalse() {
        List<User> pendingUsers = userRepository.findByStatusAndAddedToNotificationFalse(UserStatus.PENDING);
        return pendingUsers;
    }

    public User updateUser(UUID userId, User user) {
        User existingUser = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!existingUser.getUsername().equals(user.getUsername())) {
            List<StockAuditLog> stockAuditLogs = stockAuditLogService.getStockAuditLogsByUsername(existingUser.getUsername());

            for (StockAuditLog stockAuditLog : stockAuditLogs) {
                stockAuditLog.setUsername(user.getUsername());
                stockAuditLogService.saveStockAuditLog(stockAuditLog);
            }
        }

        user.setId(existingUser.getId());
        return userRepository.save(user);
    }

    public void setUserAddedToNotificationTrue(UUID id) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setAddedToNotification(true);
        userRepository.save(user);
    }

    public User acceptUser(UUID userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setStatus(UserStatus.ACTIVE);
        return userRepository.save(user);
    }

    public User rejectUser(UUID userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setStatus(UserStatus.REJECTED);
        return userRepository.save(user);
    }

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}

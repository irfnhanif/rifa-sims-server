package io.github.irfnhanif.rifasims.service;

import io.github.irfnhanif.rifasims.dto.EditUserRequest;
import io.github.irfnhanif.rifasims.entity.*;
import io.github.irfnhanif.rifasims.exception.AccessDeniedException;
import io.github.irfnhanif.rifasims.exception.BadRequestException;
import io.github.irfnhanif.rifasims.exception.InvalidCredentialsException;
import io.github.irfnhanif.rifasims.exception.ResourceNotFoundException;
import io.github.irfnhanif.rifasims.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

    public List<User> getAllUsers(String name, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page, size);
        if (name != null) {
            return userRepository.findByUsernameContainingIgnoreCaseAndDeletedFalse(name, pageable).getContent();
        }
        return userRepository.findByDeletedFalse(pageable).getContent();
    }

    public List<User> getPendingUsersAndAddedToNotificationFalse() {
        List<User> pendingUsers = userRepository.findByStatusAndAddedToNotificationFalse(UserStatus.PENDING);
        return pendingUsers;
    }

    public User getUserByUsername(String username) {
        return userRepository.findByUsernameIgnoreCase(username).orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    public User updateUser(UUID userId, EditUserRequest editUserRequest) {
        User existingUser = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!getCurrentUser().getId().equals(existingUser.getId())) {
            throw new AccessDeniedException("You are not allowed to update other user account");
        }

        existingUser.setUsername(editUserRequest.getUsername());
        existingUser.setBranch(editUserRequest.getBranch());
        return userRepository.save(existingUser);
    }

    public void setUserAddedToNotificationTrue(UUID id) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setAddedToNotification(true);
        userRepository.save(user);
    }

    public User acceptUser(UUID userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (getCurrentUser().getRole() != UserRole.OWNER) {
            throw new AccessDeniedException("You are not allowed to accept other employee account");
        }

        user.setStatus(UserStatus.ACTIVE);
        return userRepository.save(user);
    }

    public User rejectUser(UUID userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (getCurrentUser().getRole() != UserRole.OWNER) {
            throw new AccessDeniedException("You are not allowed to reject other employee account");
        }

        deleteUser(userId);
        return user;
    }

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    public void deleteUser(UUID userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (getCurrentUser().getRole() != UserRole.OWNER) {
            throw new AccessDeniedException("You are not allowed to delete other employee account");
        }

        user.setDeleted(true);
        userRepository.save(user);
    }
}

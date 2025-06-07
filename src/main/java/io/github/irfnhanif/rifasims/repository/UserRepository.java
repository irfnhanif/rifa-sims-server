package io.github.irfnhanif.rifasims.repository;

import io.github.irfnhanif.rifasims.entity.User;
import io.github.irfnhanif.rifasims.entity.UserRole;
import io.github.irfnhanif.rifasims.entity.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.net.URLConnection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByUsername(String username);
    List<User> findByStatus(UserStatus status);

    List<User> findByStatusAndAddedToNotificationFalse(UserStatus userStatus);

    Page<User> findByUsernameContainingIgnoreCase(String name, Pageable pageable);

    boolean existsByUsername(String username);
}

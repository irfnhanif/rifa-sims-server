package io.github.irfnhanif.rifasims.repository;

import io.github.irfnhanif.rifasims.entity.NotificationType;
import io.github.irfnhanif.rifasims.entity.SystemNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SystemNotificationRepository extends JpaRepository<SystemNotification, UUID> {

    boolean existsByTypeAndReferenceIdAndReadFalseAndCreatedAtAfter(NotificationType notificationType, UUID id, LocalDateTime localDateTime);

    Optional<SystemNotification> findById(UUID id);

    List<SystemNotification> findAllByOrderByReadAscCreatedAtDesc();

    List<SystemNotification> findByReadFalse();
}

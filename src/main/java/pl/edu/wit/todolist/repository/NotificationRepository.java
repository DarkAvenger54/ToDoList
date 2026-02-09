package pl.edu.wit.todolist.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import pl.edu.wit.todolist.entity.NotificationEntity;
import pl.edu.wit.todolist.entity.UserEntity;

import java.time.LocalDateTime;

public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {

    Page<NotificationEntity> findAllByUserOrderByCreatedAtDesc(UserEntity user, Pageable pageable);

    Page<NotificationEntity> findAllByUserAndReadFalseOrderByCreatedAtDesc(UserEntity user, Pageable pageable);

    long countByUserAndReadFalse(UserEntity user);

    long deleteByUserAndReadTrue(UserEntity user);

    void deleteAllByUserAndCreatedAtBefore(UserEntity user, LocalDateTime before);
}

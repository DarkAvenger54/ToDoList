package pl.edu.wit.todolist.entity;

import jakarta.persistence.*;
import lombok.*;
import pl.edu.wit.todolist.enums.NotificationType;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications", indexes = {
        @Index(name = "idx_notifications_user", columnList = "user_id"),
        @Index(name = "idx_notifications_read", columnList = "read"),
        @Index(name = "idx_notifications_created", columnList = "createdAt")
})
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class NotificationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 64)
    private NotificationType type;

    @Column(nullable = false, length = 140)
    private String title;

    @Column(nullable = false, length = 1000)
    private String message;

    @Column
    private Long refId;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private boolean read;

    @PrePersist
    void prePersist() {
        createdAt = LocalDateTime.now();
        if (title != null) title = title.trim();
        if (message != null) message = message.trim();
        // по умолчанию не прочитано
        read = false;
    }
}

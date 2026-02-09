package pl.edu.wit.todolist.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.wit.todolist.dto.NotificationResponseDto;
import pl.edu.wit.todolist.entity.NotificationEntity;
import pl.edu.wit.todolist.entity.UserEntity;
import pl.edu.wit.todolist.enums.NotificationType;
import pl.edu.wit.todolist.exception.UserNotFoundException;
import pl.edu.wit.todolist.repository.NotificationRepository;
import pl.edu.wit.todolist.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private UserEntity currentUser(org.springframework.security.core.Authentication auth) {
        return userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    private NotificationResponseDto toDto(NotificationEntity n) {
        return new NotificationResponseDto(
                n.getId(),
                n.getType(),
                n.getTitle(),
                n.getMessage(),
                n.getRefId(),
                n.getCreatedAt(),
                n.isRead()
        );
    }

    @Transactional
    public NotificationEntity notifyUser(
            UserEntity user,
            NotificationType type,
            String title,
            String message,
            Long refId,
            boolean sendEmail
    ) {
        NotificationEntity n = NotificationEntity.builder()
                .user(user)
                .type(type)
                .title(title)
                .message(message)
                .refId(refId)
                .read(false)
                .build();

        NotificationEntity saved = notificationRepository.save(n);

        if (sendEmail) {
            try {
                String subject = "[" + type.name() + "] " + title;
                emailService.sendNotificationEmail(
                        user.getEmail(),
                        subject,
                        title,
                        message
                );
            } catch (Exception e) {
                log.warn("Failed to send notification email to {}", user.getEmail(), e);
            }
        }
        return saved;
    }

    @Transactional(readOnly = true)
    public Page<NotificationResponseDto> list(
            Authentication auth,
            boolean unreadOnly,
            Pageable pageable
    ) {
        UserEntity user = currentUser(auth);

        Page<NotificationEntity> page = unreadOnly
                ? notificationRepository.findAllByUserAndReadFalseOrderByCreatedAtDesc(user, pageable)
                : notificationRepository.findAllByUserOrderByCreatedAtDesc(user, pageable);

        return page.map(this::toDto);
    }

    @Transactional(readOnly = true)
    public long unreadCount(Authentication auth) {
        UserEntity user = currentUser(auth);
        return notificationRepository.countByUserAndReadFalse(user);
    }

    @Transactional
    public void markRead(Authentication auth, Long notificationId) {
        UserEntity user = currentUser(auth);

        NotificationEntity n = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found"));

        if (!n.getUser().getId().equals(user.getId())) {
            throw new SecurityException("No access");
        }

        if (!n.isRead()) {
            n.setRead(true);
        }
    }

    @Transactional
    public void markAllRead(Authentication auth) {
        UserEntity user = currentUser(auth);

        notificationRepository
                .findAllByUserAndReadFalseOrderByCreatedAtDesc(user, Pageable.unpaged())
                .forEach(n -> n.setRead(true));
    }

    @Transactional
    public long deleteRead(Authentication auth) {
        UserEntity user = currentUser(auth);
        return notificationRepository.deleteByUserAndReadTrue(user);
    }
}

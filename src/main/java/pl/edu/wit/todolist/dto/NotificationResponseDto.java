package pl.edu.wit.todolist.dto;

import pl.edu.wit.todolist.enums.NotificationType;

import java.time.Instant;

public record NotificationResponseDto(
        Long id,
        NotificationType type,
        String title,
        String message,
        Long refId,
        Instant createdAt,
        boolean read
) {}

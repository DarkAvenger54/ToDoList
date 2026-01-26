package pl.edu.wit.todolist.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pl.edu.wit.todolist.dto.MarkReadRequestDto;
import pl.edu.wit.todolist.dto.NotificationResponseDto;
import pl.edu.wit.todolist.service.NotificationService;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public Page<NotificationResponseDto> list(
            @RequestParam(defaultValue = "true") boolean unreadOnly,
            @ParameterObject Pageable pageable,
            Authentication auth
    ) {
        return notificationService.list(auth, unreadOnly, pageable);
    }

    @GetMapping("/unread-count")
    public long unreadCount(Authentication auth) {
        return notificationService.unreadCount(auth);
    }

    @PostMapping("/mark-read")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void markRead(@Valid @RequestBody MarkReadRequestDto dto, Authentication auth) {
        notificationService.markRead(auth, dto.notificationId());
    }

    @PostMapping("/mark-all-read")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void markAllRead(Authentication auth) {
        notificationService.markAllRead(auth);
    }
}

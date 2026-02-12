package pl.edu.wit.todolist.dto.group;

import pl.edu.wit.todolist.enums.GroupInviteStatus;

import java.time.LocalDateTime;

public record GroupInviteResponseDto(
        Long id,
        Long groupId,
        String groupName,
        String inviterUsername,
        GroupInviteStatus status,
        LocalDateTime createdAt
) {}
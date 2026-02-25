package pl.edu.wit.todolist.dto.group;

import pl.edu.wit.todolist.enums.GroupInviteStatus;

import java.time.Instant;

public record GroupInviteResponseDto(
        Long id,
        Long groupId,
        String groupName,
        String inviterUsername,
        GroupInviteStatus status,
        Instant createdAt
) {}

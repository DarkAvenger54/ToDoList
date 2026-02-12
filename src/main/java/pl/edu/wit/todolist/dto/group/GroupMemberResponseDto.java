package pl.edu.wit.todolist.dto.group;

import pl.edu.wit.todolist.enums.GroupRole;

public record GroupMemberResponseDto(
        Long userId,
        String username,
        String email,
        GroupRole role
) {}

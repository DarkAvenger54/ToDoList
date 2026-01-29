package pl.edu.wit.todolist.dto;

import jakarta.validation.constraints.NotBlank;
import pl.edu.wit.todolist.enums.GroupRole;

public record GroupSetRoleRequestDto(
        @NotBlank String username,
        GroupRole role // ADMIN или MEMBER (OWNER запрещаем)
) {}

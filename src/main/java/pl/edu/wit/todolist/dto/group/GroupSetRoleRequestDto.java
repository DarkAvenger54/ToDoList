package pl.edu.wit.todolist.dto.group;

import jakarta.validation.constraints.NotBlank;
import pl.edu.wit.todolist.enums.GroupRole;

public record GroupSetRoleRequestDto(
        @NotBlank String username,
        GroupRole role
) {}

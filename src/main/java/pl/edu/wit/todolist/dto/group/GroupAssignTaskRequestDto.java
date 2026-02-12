package pl.edu.wit.todolist.dto.group;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import pl.edu.wit.todolist.enums.TaskPriority;

import java.time.LocalDateTime;

public record GroupAssignTaskRequestDto(
        @NotBlank String assigneeUsername,
        @NotBlank @Size(max = 140) String title,
        @Size(max = 4000) String description,
        TaskPriority priority,
        LocalDateTime dueAt,
        boolean visibleInGroup
) {}

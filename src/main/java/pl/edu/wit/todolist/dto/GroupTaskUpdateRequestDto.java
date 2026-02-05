package pl.edu.wit.todolist.dto;

import jakarta.validation.constraints.Size;
import pl.edu.wit.todolist.enums.TaskPriority;
import pl.edu.wit.todolist.enums.TaskStatus;

import java.time.LocalDateTime;

public record GroupTaskUpdateRequestDto(
        @Size(max = 140) String title,
        @Size(max = 4000) String description,
        TaskStatus status,
        TaskPriority priority,
        LocalDateTime dueAt,
        Boolean clearDueAt,
        Boolean visibleInGroup
) {}

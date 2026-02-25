package pl.edu.wit.todolist.dto.task;

import pl.edu.wit.todolist.enums.TaskPriority;
import pl.edu.wit.todolist.enums.TaskScope;
import pl.edu.wit.todolist.enums.TaskStatus;

import java.time.Instant;


public record TaskResponseDto(
        Long id,
        String title,
        String description,
        TaskStatus status,
        TaskPriority priority,
        TaskScope scope,
        Instant dueAt,
        Instant createdAt,
        Instant updatedAt,
        String creatorDisplayName,
        Long groupId,
        String groupName,
        String assigneeUsername,
        boolean groupTask,
        boolean visibleInGroup,
        boolean overdue
) {}

package pl.edu.wit.todolist.dto;

import pl.edu.wit.todolist.enums.TaskPriority;
import pl.edu.wit.todolist.enums.TaskScope;
import pl.edu.wit.todolist.enums.TaskStatus;

import java.time.LocalDateTime;


public record TaskResponseDto(
        Long id,
        String title,
        String description,
        TaskStatus status,
        TaskPriority priority,
        TaskScope scope,
        LocalDateTime dueAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String creatorDisplayName,
        Long groupId,
        String groupName,
        String assigneeUsername,
        boolean groupTask,
        boolean visibleInGroup
) {}

package pl.edu.wit.todolist.dto;

import jakarta.validation.constraints.NotNull;
import pl.edu.wit.todolist.enums.TaskStatus;

public record GroupTaskUpdateStatusRequestDto(
        @NotNull TaskStatus status
) {}
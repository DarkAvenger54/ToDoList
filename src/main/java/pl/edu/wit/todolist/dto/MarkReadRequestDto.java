package pl.edu.wit.todolist.dto;

import jakarta.validation.constraints.NotNull;

public record MarkReadRequestDto(
        @NotNull Long notificationId
) {}
package pl.edu.wit.todolist.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record GroupCreateRequestDto(
        @NotBlank @Size(max = 80) String name
) {}

package pl.edu.wit.todolist.dto.group;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record GroupRenameRequestDto(
        @NotBlank @Size(max = 80) String name
) {}
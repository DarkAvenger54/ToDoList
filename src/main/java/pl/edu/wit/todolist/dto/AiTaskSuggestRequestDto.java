package pl.edu.wit.todolist.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import pl.edu.wit.todolist.enums.TaskScope;

public record AiTaskSuggestRequestDto(
        @NotBlank @Size(max = 2000) String command,
        TaskScope scope,          // optional: PERSONAL/GROUP (если null -> PERSONAL)
        Long groupId,             // optional: для scope=GROUP
        Integer maxTasks          // optional: default 5
) {}

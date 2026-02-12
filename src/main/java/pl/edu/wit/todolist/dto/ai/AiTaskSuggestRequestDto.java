package pl.edu.wit.todolist.dto.ai;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import pl.edu.wit.todolist.enums.TaskScope;

public record AiTaskSuggestRequestDto(
        @NotBlank @Size(max = 2000) String command,
        TaskScope scope,
        Long groupId,
        Integer maxTasks
) {}

package pl.edu.wit.todolist.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pl.edu.wit.todolist.dto.TaskCreateRequestDto;
import pl.edu.wit.todolist.dto.TaskResponseDto;
import pl.edu.wit.todolist.dto.TaskUpdateRequestDto;
import pl.edu.wit.todolist.enums.TaskStatus;
import pl.edu.wit.todolist.service.TaskService;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {
    private final TaskService taskService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TaskResponseDto create(@Valid @RequestBody TaskCreateRequestDto dto, Authentication auth) {
        return taskService.create(auth, dto);
    }

    @GetMapping
    public Page<TaskResponseDto> list(
            @RequestParam(required = false) TaskStatus status,
            @ParameterObject Pageable pageable,
            Authentication auth
    ) {
        return taskService.list(auth, status, pageable);
    }

    @GetMapping("/{id}")
    public TaskResponseDto get(@PathVariable Long id, Authentication auth) {
        return taskService.get(auth, id);
    }

    @PutMapping("/{id}")
    public TaskResponseDto update(@PathVariable Long id,
                                  @Valid @RequestBody TaskUpdateRequestDto dto,
                                  Authentication auth) {
        return taskService.update(auth, id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, Authentication auth) {
        taskService.delete(auth, id);
    }
}

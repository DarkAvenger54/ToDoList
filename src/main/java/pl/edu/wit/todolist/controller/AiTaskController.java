package pl.edu.wit.todolist.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pl.edu.wit.todolist.dto.AiTaskSuggestRequestDto;
import pl.edu.wit.todolist.dto.AiTaskSuggestionList;
import pl.edu.wit.todolist.service.AiTaskService;

@RestController
@RequestMapping("/api/ai/tasks")
@RequiredArgsConstructor
public class AiTaskController {

    private final AiTaskService aiTaskService;

    @PostMapping("/suggest")
    @ResponseStatus(HttpStatus.OK)
    public AiTaskSuggestionList suggest(
            @Valid @RequestBody AiTaskSuggestRequestDto req,
            Authentication auth
    ) {
        return aiTaskService.suggest(req);
    }
}

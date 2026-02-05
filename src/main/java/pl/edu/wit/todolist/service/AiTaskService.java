package pl.edu.wit.todolist.service;

import com.openai.client.OpenAIClient;
import com.openai.models.ChatModel;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.StructuredResponse;
import com.openai.models.responses.StructuredResponseCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.edu.wit.todolist.config.AiProperties;
import pl.edu.wit.todolist.dto.AiTaskSuggestRequestDto;
import pl.edu.wit.todolist.dto.AiTaskSuggestionList;
import pl.edu.wit.todolist.enums.TaskScope;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AiTaskService {

    private final OpenAIClient client;
    private final AiProperties aiProperties;

    public AiTaskSuggestionList suggest(AiTaskSuggestRequestDto req) {
        int maxTasks = (req.maxTasks() == null || req.maxTasks() <= 0) ? 5 : Math.min(req.maxTasks(), 15);
        TaskScope scope = (req.scope() == null) ? TaskScope.PERSONAL : req.scope();

        String now = LocalDateTime.now().toString();

        String system = """
                You are an assistant that converts a user's natural-language command into a small list of ToDo tasks.
                Output MUST match the JSON schema exactly.

                Rules:
                - Produce %d tasks maximum.
                - title <= 140 chars, description <= 4000 chars.
                - priority must be one of LOW, MEDIUM, HIGH, URGENT (default MEDIUM if unclear).
                - dueAtIso: ISO-8601 local datetime like 2026-02-05T18:00, or empty string if missing.
                - Language: keep titles/descriptions in the same language as the user's command.
                Current server time: %s
                Scope: %s
                """.formatted(maxTasks, now, scope.name());

        String user = "Command: " + req.command();

        StructuredResponseCreateParams<AiTaskSuggestionList> params =
                ResponseCreateParams.builder()
                        .model(ChatModel.of(aiProperties.model()))
                        .instructions(system)   // <-- вместо InputItem.ofSystem
                        .input(user)            // <-- вместо InputItem.ofUser
                        .maxOutputTokens(aiProperties.maxOutputTokens() == null ? 1600 : aiProperties.maxOutputTokens())
                        .text(AiTaskSuggestionList.class) // <-- включает structured outputs
                        .build();

        StructuredResponse<AiTaskSuggestionList> resp = client.responses().create(params);

        return resp.output().stream()
                .flatMap(item -> item.message().stream())
                .flatMap(message -> message.content().stream())
                .flatMap(content -> content.outputText().stream()) // тут уже AiTaskSuggestionList
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Model returned no structured output"));
    }
}

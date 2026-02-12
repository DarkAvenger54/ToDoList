package pl.edu.wit.todolist.dto.ai;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import java.util.List;

public class AiTaskSuggestionList {

    @JsonPropertyDescription("List of task suggestions")
    public List<AiTaskSuggestion> tasks;

    @JsonPropertyDescription("One-sentence summary of what was understood from the command")
    public String summary;
}

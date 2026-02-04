package pl.edu.wit.todolist.dto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import pl.edu.wit.todolist.enums.TaskPriority;

public class AiTaskSuggestion {

    @JsonPropertyDescription("Short title up to 140 chars")
    public String title;

    @JsonPropertyDescription("Optional description up to 4000 chars")
    public String description;

    @JsonPropertyDescription("Task priority: LOW/MEDIUM/HIGH/URGENT")
    public TaskPriority priority;

    @JsonPropertyDescription("Due date-time in ISO-8601, or empty if not specified (e.g. 2026-02-05T18:00)")
    public String dueAtIso; // оставим строкой, потом аккуратно парсим
}
package pl.edu.wit.todolist.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.ai")
public record AiProperties(
        String model,
        Integer maxOutputTokens
) {}

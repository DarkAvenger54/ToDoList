package pl.edu.wit.todolist.config;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAiClientConfig {

    @Bean
    public OpenAIClient openAIClient(org.springframework.core.env.Environment env) {
        // ты уже держишь ключ в openai.api.key=${OPENAI_API_KEY}
        // SDK умеет брать ключ из env, но тут явно из spring property:
        String apiKey = env.getProperty("openai.api.key");
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("Missing openai.api.key (set OPENAI_API_KEY env var)");
        }
        return OpenAIOkHttpClient.builder()
                .apiKey(apiKey)
                .build();
    }
}
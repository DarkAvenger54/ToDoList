package pl.edu.wit.todolist.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    private static final String API_TITLE = "ToDoList API";
    private static final String API_VERSION = "v1";
    private static final String API_DESCRIPTION = "API documentation for ToDoList";

    private static final String SECURITY_SCHEME_NAME = "Bearer Authentication";
    private static final String AUTH_HEADER = "Authorization";
    private static final String SCHEME = "bearer";
    private static final String TOKEN_FORMAT = "JWT";

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title(API_TITLE)
                        .version(API_VERSION)
                        .description(API_DESCRIPTION))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME,
                                new SecurityScheme()
                                        .name(AUTH_HEADER)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme(SCHEME)
                                        .bearerFormat(TOKEN_FORMAT)));
    }
}

package pl.edu.wit.todolist;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableScheduling
public class ToDoListApplication {

    public static void main(String[] args) {
        try {
            Dotenv dotenv = Dotenv.configure()
                    .ignoreIfMissing()
                    .load();
            dotenv.entries().forEach(e -> System.setProperty(e.getKey(), e.getValue()));
        } catch (Exception ignored) {}
        SpringApplication.run(ToDoListApplication.class, args);
    }

}

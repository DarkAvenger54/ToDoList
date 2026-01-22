package pl.edu.wit.todolist.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;
    @Value("${app.mail.from}")
    private String from;
    public void sendEmailConfirmation(String to, String confirmLink) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject("Potwierdzenie email — ToDoList");
        message.setText("""
                Cześć!
                
                Dziękujemy za rejestrację. Aby potwierdzić email, kliknij link:
                %s
                
                Jeśli to nie Ty — zignoruj ten email.
                """.formatted(confirmLink));

        mailSender.send(message);
    }
}

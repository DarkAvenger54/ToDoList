package pl.edu.wit.todolist.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;
    private final EmailTemplateService templateService;

    @Value("${app.mail.from}")
    private String from;

    public void sendEmailConfirmation(String to, String confirmLink) {
        String html = templateService.render(
                "emails/email-confirmation",
                Map.of("confirmLink", confirmLink)
        );

        sendHtml(to, "Confirm your email — ToDoList", html);
    }

    public void sendPasswordChangedInfo(String to, String username, String resetStartLink) {
        String html = templateService.render(
                "emails/password-changed",
                Map.of(
                        "username", username,
                        "time", LocalDateTime.now().toString(),
                        "resetStartLink", resetStartLink
                )
        );

        sendHtml(to, "Your password was changed — ToDoList", html);
    }

    public void sendPasswordReset(String to, String resetLink) {
        String html = templateService.render(
                "emails/password-reset",
                Map.of("resetLink", resetLink)
        );

        sendHtml(to, "Reset your password — ToDoList", html);
    }

    private void sendHtml(String to, String subject, String html) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    mimeMessage,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name()
            );

            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);

            mailSender.send(mimeMessage);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to send email", e);
        }
    }
}

package pl.edu.wit.todolist.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.wit.todolist.entity.UserEntity;
import pl.edu.wit.todolist.enums.EmailTokenType;
import pl.edu.wit.todolist.exception.UserNotFoundException;
import pl.edu.wit.todolist.repository.UserRepository;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class EmailConfirmationService {
    private final EmailTokenService emailTokenService;
    private final EmailService emailService;
    private final UserRepository userRepository;
    @Value("${app.base-url:http://localhost:8085}")
    private String baseUrl;

    @Value("${app.email-confirm.ttl-minutes:60}")
    private long ttlMinutes;

    @Transactional
    public void sendConfirmation(UserEntity user) {
        var tokenEntity = emailTokenService.createToken(
                user,
                EmailTokenType.EMAIL_CONFIRMATION,
                Duration.ofMinutes(ttlMinutes),
                null
        );

        String link = baseUrl + "/api/auth/confirm-email?token=" + tokenEntity.getToken();
        emailService.sendEmailConfirmation(user.getEmail(), link);
    }
    @Transactional
    public void confirmEmail(String token) {
        var t = emailTokenService.validate(token, EmailTokenType.EMAIL_CONFIRMATION);

        UserEntity user = userRepository.findById(t.getUser().getId())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        user.setEmailVerified(true);
        t.setUsed(true);
    }
}

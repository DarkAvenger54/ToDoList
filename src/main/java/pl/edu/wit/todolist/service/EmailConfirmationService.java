package pl.edu.wit.todolist.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.wit.todolist.entity.UserEntity;
import pl.edu.wit.todolist.enums.EmailTokenType;
import pl.edu.wit.todolist.repository.EmailTokenRepository;
import pl.edu.wit.todolist.repository.UserRepository;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class EmailConfirmationService {

    private final EmailTokenService emailTokenService;
    private final EmailTokenRepository emailTokenRepository;
    private final EmailService emailService;
    private final UserRepository userRepository;

    @Value("${app.base-url:http://localhost:8085}")
    private String baseUrl;

    @Value("${app.email-confirm.ttl-minutes:60}")
    private long ttlMinutes;

    @Value("${app.email-confirm.resend-cooldown-seconds:60}")
    private long resendCooldownSeconds;

    @Transactional
    public void sendConfirmationIfAllowed(UserEntity user) {
        if (user.isEmailVerified()) return;

        LocalDateTime last = user.getLastEmailConfirmationSentAt();
        if (last != null && last.plusSeconds(resendCooldownSeconds).isAfter(LocalDateTime.now())) {
            return;
        }

        emailTokenRepository.deleteAllByUserAndType(user, EmailTokenType.EMAIL_CONFIRMATION);

        var pair = emailTokenService.createToken(
                user,
                EmailTokenType.EMAIL_CONFIRMATION,
                Duration.ofMinutes(ttlMinutes),
                null
        );

        String link = baseUrl + "/api/auth/confirm-email?token=" + pair.rawToken();
        emailService.sendEmailConfirmation(user.getEmail(), link);

        user.setLastEmailConfirmationSentAt(LocalDateTime.now());
        userRepository.save(user);
    }

    @Transactional
    public void confirmEmail(String rawToken) {
        var t = emailTokenService.validateRawToken(rawToken, EmailTokenType.EMAIL_CONFIRMATION);
        t.getUser().setEmailVerified(true);
        t.setUsed(true);
    }
}

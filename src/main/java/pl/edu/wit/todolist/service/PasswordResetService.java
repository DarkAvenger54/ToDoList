package pl.edu.wit.todolist.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.wit.todolist.entity.UserEntity;
import pl.edu.wit.todolist.enums.EmailTokenType;
import pl.edu.wit.todolist.repository.EmailTokenRepository;
import pl.edu.wit.todolist.repository.UserRepository;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final UserRepository userRepository;
    private final EmailTokenRepository emailTokenRepository;
    private final EmailTokenService emailTokenService;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.frontend-base-url:http://localhost:8085}")
    private String frontendBaseUrl;

    @Value("${app.password-reset.ttl-minutes:30}")
    private long ttlMinutes;

    @Transactional
    public void startReset(String email) {
        if (email == null || email.isBlank()) return;

        userRepository.findByEmail(email.trim().toLowerCase()).ifPresent(user -> {
            // Only one active reset token per user
            emailTokenRepository.deleteAllByUserAndType(user, EmailTokenType.PASSWORD_RESET);

            EmailTokenService.TokenPair pair = emailTokenService.createToken(
                    user,
                    EmailTokenType.PASSWORD_RESET,
                    Duration.ofMinutes(ttlMinutes),
                    null
            );

            String resetLink = frontendBaseUrl + "/reset-password.html?token=" + pair.rawToken();

            emailService.sendPasswordReset(user.getEmail(), resetLink);
        });
    }

    @Transactional
    public void resetPassword(String rawToken, String newPassword) {
        var tokenEntity = emailTokenService.validateRawToken(rawToken, EmailTokenType.PASSWORD_RESET);

        UserEntity user = tokenEntity.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));

        tokenEntity.setUsed(true);

        emailTokenRepository.deleteAllByUserAndType(user, EmailTokenType.PASSWORD_RESET);

        String resetStartLink = frontendBaseUrl + "/forgot-password.html";
        emailService.sendPasswordChangedInfo(user.getEmail(), user.getUsername(), resetStartLink);
    }
}

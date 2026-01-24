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

    /**
     * Base URL for "frontend pages" (in dev: backend-served static HTML,
     * later: real frontend domain).
     */
    @Value("${app.frontend-base-url:http://localhost:8085}")
    private String frontendBaseUrl;

    @Value("${app.password-reset.ttl-minutes:30}")
    private long ttlMinutes;

    /**
     * Start password reset flow.
     * Must always be "silent": if email not found, do nothing (no errors).
     */
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

            // IMPORTANT: link to frontend page (dev html now, real FE later)
            String resetLink = frontendBaseUrl + "/reset-password?token=" + pair.rawToken();

            emailService.sendPasswordReset(user.getEmail(), resetLink);
        });
    }

    @Transactional
    public void resetPassword(String rawToken, String newPassword) {
        var tokenEntity = emailTokenService.validateRawToken(rawToken, EmailTokenType.PASSWORD_RESET);

        UserEntity user = tokenEntity.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));

        tokenEntity.setUsed(true);

        // cleanup all reset tokens after successful reset
        emailTokenRepository.deleteAllByUserAndType(user, EmailTokenType.PASSWORD_RESET);

        // optional: send info email with "start reset again" link
        String resetStartLink = frontendBaseUrl + "/forgot-password";
        emailService.sendPasswordChangedInfo(user.getEmail(), user.getUsername(), resetStartLink);
    }
}

package pl.edu.wit.todolist.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.wit.todolist.entity.EmailTokenEntity;
import pl.edu.wit.todolist.entity.UserEntity;
import pl.edu.wit.todolist.enums.EmailTokenType;
import pl.edu.wit.todolist.exception.InvalidEmailTokenException;
import pl.edu.wit.todolist.repository.EmailTokenRepository;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class EmailTokenService {
    private final EmailTokenRepository repository;
    private final TokenHashingService hashingService;

    public record TokenPair(String rawToken, EmailTokenEntity entity) {}

    @Transactional
    public TokenPair createToken(UserEntity user, EmailTokenType type, Duration ttl, String targetEmail) {
        String raw = generateRaw();
        String hash = hashingService.sha256Hex(raw);

        EmailTokenEntity token = EmailTokenEntity.builder()
                .tokenHash(hash)
                .type(type)
                .user(user)
                .targetEmail(targetEmail)
                .expiresAt(LocalDateTime.now().plus(ttl))
                .used(false)
                .createdAt(LocalDateTime.now())
                .build();

        return new TokenPair(raw, repository.save(token));
    }

    @Transactional(readOnly = true)
    public EmailTokenEntity validateRawToken(String rawToken, EmailTokenType type) {
        if (rawToken == null || rawToken.isBlank()) {
            throw new InvalidEmailTokenException("Invalid token");
        }

        String hash = hashingService.sha256Hex(rawToken);

        EmailTokenEntity t = repository.findByTokenHash(hash)
                .orElseThrow(() -> new InvalidEmailTokenException("Invalid token"));

        if (t.isUsed()) throw new InvalidEmailTokenException("Token already used");
        if (t.getExpiresAt().isBefore(LocalDateTime.now()))
            throw new InvalidEmailTokenException("Token expired");
        if (t.getType() != type)
            throw new InvalidEmailTokenException("Invalid token type");

        return t;
    }

    private String generateRaw() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}

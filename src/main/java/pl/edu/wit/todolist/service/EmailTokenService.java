package pl.edu.wit.todolist.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.edu.wit.todolist.entity.EmailTokenEntity;
import pl.edu.wit.todolist.entity.UserEntity;
import pl.edu.wit.todolist.enums.EmailTokenType;
import pl.edu.wit.todolist.repository.EmailTokenRepository;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class EmailTokenService {
    private final EmailTokenRepository repository;

    public EmailTokenEntity createToken(UserEntity user, EmailTokenType type, Duration ttl, String targetEmail) {
        EmailTokenEntity token = EmailTokenEntity.builder()
                .token(generate())
                .type(type)
                .user(user)
                .targetEmail(targetEmail)
                .expiresAt(LocalDateTime.now().plus(ttl))
                .used(false)
                .build();

        return repository.save(token);
    }

    public EmailTokenEntity validate(String token, EmailTokenType type) {
        EmailTokenEntity t = repository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid token"));

        if (t.isUsed()) throw new RuntimeException("Token used");
        if (t.getExpiresAt().isBefore(LocalDateTime.now()))
            throw new RuntimeException("Token expired");
        if (t.getType() != type)
            throw new RuntimeException("Invalid token type");

        return t;
    }

    private String generate() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}

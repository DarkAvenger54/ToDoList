package pl.edu.wit.todolist.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.edu.wit.todolist.entity.EmailTokenEntity;
import pl.edu.wit.todolist.entity.UserEntity;
import pl.edu.wit.todolist.enums.EmailTokenType;

import java.util.Optional;

public interface EmailTokenRepository extends JpaRepository<EmailTokenEntity, Long> {
    Optional<EmailTokenEntity> findByTokenHash(String tokenHash);
    void deleteAllByUserAndType(UserEntity user, EmailTokenType type);
}

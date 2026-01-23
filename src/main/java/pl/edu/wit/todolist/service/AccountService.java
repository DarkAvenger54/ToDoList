package pl.edu.wit.todolist.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.wit.todolist.entity.UserEntity;
import pl.edu.wit.todolist.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final UserService userService;
    private final UserRepository userRepository;
    private final EmailConfirmationService emailConfirmationService;

    @Transactional
    public UserEntity register(String email, String username, String password) {
        UserEntity user = userService.createUser(email, username, password);
        emailConfirmationService.sendConfirmationIfAllowed(user);
        return user;
    }

    @Transactional
    public void confirmEmail(String rawToken) {
        emailConfirmationService.confirmEmail(rawToken);
    }
    @Transactional
    public void resendEmailConfirmation(String email) {
        userRepository.findByEmail(email)
                .ifPresent(emailConfirmationService::sendConfirmationIfAllowed);
    }
}

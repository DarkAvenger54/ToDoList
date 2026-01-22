package pl.edu.wit.todolist.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import pl.edu.wit.todolist.dto.JwtResponseDto;
import pl.edu.wit.todolist.dto.LoginRequestDto;
import pl.edu.wit.todolist.dto.RegistrationRequestDto;
import pl.edu.wit.todolist.dto.UserResponseDto;
import pl.edu.wit.todolist.entity.UserEntity;
import pl.edu.wit.todolist.service.AuthService;
import pl.edu.wit.todolist.service.EmailConfirmationService;
import pl.edu.wit.todolist.service.UserService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final EmailConfirmationService emailConfirmationService;

    private final UserService userService;
    @PostMapping("/register")
    public UserResponseDto register(@Valid @RequestBody RegistrationRequestDto registrationRequestDto) {
        UserEntity user = userService.createUser(registrationRequestDto.email(), registrationRequestDto.username(), registrationRequestDto.password());
        emailConfirmationService.sendConfirmation(user);
        return new UserResponseDto(user.getUsername(), user.getEmail());
    }

    @PostMapping("/login")
    public JwtResponseDto login(@Valid @RequestBody LoginRequestDto loginRequestDto) {
        String jwt = authService.generateJwtToken(loginRequestDto.login(), loginRequestDto.password());
        return new JwtResponseDto(jwt);
    }
    @GetMapping("/confirm-email")
    public String confirmEmail(@RequestParam String token) {
        emailConfirmationService.confirmEmail(token);
        return "Email confirmed";
    }
}

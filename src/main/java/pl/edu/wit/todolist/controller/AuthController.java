package pl.edu.wit.todolist.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.wit.todolist.dto.JwtResponseDto;
import pl.edu.wit.todolist.dto.LoginRequestDto;
import pl.edu.wit.todolist.dto.RegistrationRequestDto;
import pl.edu.wit.todolist.dto.UserResponseDto;
import pl.edu.wit.todolist.entity.UserEntity;
import pl.edu.wit.todolist.service.AuthService;
import pl.edu.wit.todolist.service.UserService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    private final UserService userService;
    @PostMapping("/register")
    public UserResponseDto register(@Valid @RequestBody RegistrationRequestDto registrationRequestDto) {
        UserEntity user = userService.createUser(registrationRequestDto.email(), registrationRequestDto.username(), registrationRequestDto.password());
        return new UserResponseDto(user.getUsername(), user.getEmail());
    }

    @PostMapping("/login")
    public JwtResponseDto login(@Valid @RequestBody LoginRequestDto loginRequestDto) {
        String jwt = authService.generateJwtToken(loginRequestDto.username(), loginRequestDto.password());
        return new JwtResponseDto(jwt);
    }
}

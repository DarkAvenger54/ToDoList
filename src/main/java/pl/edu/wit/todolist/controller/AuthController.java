package pl.edu.wit.todolist.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pl.edu.wit.todolist.dto.*;
import pl.edu.wit.todolist.entity.UserEntity;
import pl.edu.wit.todolist.service.AccountService;
import pl.edu.wit.todolist.service.AuthService;


@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final AccountService accountService;

    @PostMapping("/register")
    public UserResponseDto register(@Valid @RequestBody RegistrationRequestDto dto) {
        UserEntity user = accountService.register(dto.email(), dto.username(), dto.password());
        return new UserResponseDto(user.getUsername(), user.getEmail());
    }

    @PostMapping("/login")
    public JwtResponseDto login(@Valid @RequestBody LoginRequestDto dto) {
        String jwt = authService.generateJwtToken(dto.login(), dto.password());
        return new JwtResponseDto(jwt);
    }

    @GetMapping("/confirm-email")
    public String confirmEmail(@RequestParam String token) {
        accountService.confirmEmail(token);
        return "Email confirmed";
    }

    @PostMapping("/resend-confirmation")
    @ResponseStatus(HttpStatus.OK)
    public void resend(@Valid @RequestBody ResendConfirmationRequestDto dto) {
        accountService.resendEmailConfirmation(dto.email());
    }
    @PostMapping("/forgot-password")
    @ResponseStatus(HttpStatus.OK)
    public void forgotPassword(@Valid @RequestBody ForgotPasswordRequestDto dto) {
        accountService.forgotPassword(dto.email());
    }

    @PostMapping("/reset-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void resetPassword(@Valid @RequestBody ResetPasswordRequestDto dto) {
        accountService.resetPassword(dto.token(), dto.newPassword());
    }
}

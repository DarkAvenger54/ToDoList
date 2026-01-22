package pl.edu.wit.todolist.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pl.edu.wit.todolist.dto.ChangePasswordRequestDto;
import pl.edu.wit.todolist.service.UserService;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    @PutMapping("/me/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(@Valid @RequestBody ChangePasswordRequestDto dto,
                               Authentication auth) {
        userService.changePassword(auth, dto.oldPassword(), dto.newPassword());
    }
}

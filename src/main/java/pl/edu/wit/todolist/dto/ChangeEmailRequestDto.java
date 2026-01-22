package pl.edu.wit.todolist.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ChangeEmailRequestDto(
        @NotBlank @Email String newEmail,
        @NotBlank String password
) {}

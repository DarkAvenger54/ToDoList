package pl.edu.wit.todolist.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequestDto(
        @NotBlank String oldPassword,
        @NotBlank @Size(min = 6, max = 100) String newPassword
) {}

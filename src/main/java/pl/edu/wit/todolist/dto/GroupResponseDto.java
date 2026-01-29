package pl.edu.wit.todolist.dto;

public record GroupResponseDto(
        Long id,
        String name,
        String ownerUsername
) {}

package pl.edu.wit.todolist.dto.group;

public record GroupResponseDto(
        Long id,
        String name,
        String ownerUsername
) {}

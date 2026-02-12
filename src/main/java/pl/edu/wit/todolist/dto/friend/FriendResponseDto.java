package pl.edu.wit.todolist.dto.friend;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FriendResponseDto {
    private Long id;
    private String username;
    private String email;
}
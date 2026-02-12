package pl.edu.wit.todolist.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pl.edu.wit.todolist.dto.friend.FriendRequestDto;
import pl.edu.wit.todolist.dto.friend.FriendResponseDto;
import pl.edu.wit.todolist.service.FriendshipService;

import java.util.List;

@RestController
@RequestMapping("/api/friends")
@RequiredArgsConstructor
public class FriendshipController {

    private final FriendshipService friendshipService;

    @Operation(summary = "Send friend request")
    @PostMapping("/request")
    @ResponseStatus(HttpStatus.OK)
    public void sendRequest(
            @RequestBody FriendRequestDto dto,
            Authentication auth
    ) {
        friendshipService.sendRequest(auth, dto.getUsername());
    }

    @Operation(summary = "Accept friend request")
    @PostMapping("/accept/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void accept(
            @PathVariable Long id,
            Authentication auth
    ) {
        friendshipService.accept(id, auth);
    }

    @Operation(summary = "Remove friend")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(
            @PathVariable Long id,
            Authentication auth
    ) {
        friendshipService.remove(id, auth);
    }

    @Operation(summary = "Get friends of authenticated user")
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<FriendResponseDto> friends(Authentication auth) {
        return friendshipService.friends(auth);
    }
}


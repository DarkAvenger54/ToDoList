package pl.edu.wit.todolist.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pl.edu.wit.todolist.dto.group.GroupInviteResponseDto;
import pl.edu.wit.todolist.service.GroupInviteService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/group-invites")
public class GroupInviteController {

    private final GroupInviteService groupInviteService;

    @GetMapping("/mine")
    public List<GroupInviteResponseDto> mine(Authentication auth) {
        return groupInviteService.myPendingInvites(auth);
    }

    @PostMapping("/{inviteId}/accept")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void accept(@PathVariable Long inviteId, Authentication auth) {
        groupInviteService.accept(auth, inviteId);
    }

    @PostMapping("/{inviteId}/reject")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reject(@PathVariable Long inviteId, Authentication auth) {
        groupInviteService.reject(auth, inviteId);
    }
}

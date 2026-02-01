package pl.edu.wit.todolist.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pl.edu.wit.todolist.dto.*;
import pl.edu.wit.todolist.service.GroupInviteService;
import pl.edu.wit.todolist.service.GroupService;

import java.util.List;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;
    private final GroupInviteService groupInviteService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GroupResponseDto create(Authentication auth, @RequestBody GroupCreateRequestDto dto) {
        return groupService.create(auth, dto);
    }

    @GetMapping
    public List<GroupResponseDto> myGroups(Authentication auth) {
        return groupService.myGroups(auth);
    }

    @PutMapping("/{groupId}/name")
    public GroupResponseDto rename(Authentication auth,
                                   @PathVariable Long groupId,
                                   @RequestBody GroupRenameRequestDto dto) {
        return groupService.rename(auth, groupId, dto);
    }

    @GetMapping("/{groupId}/members")
    public List<GroupMemberResponseDto> members(Authentication auth, @PathVariable Long groupId) {
        return groupService.members(auth, groupId);
    }

    @PostMapping("/{groupId}/invites/{username}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void invite(Authentication auth,
                       @PathVariable Long groupId,
                       @PathVariable String username) {
        groupInviteService.invite(auth, groupId, username);
    }

    @PutMapping("/{groupId}/role")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void setRole(Authentication auth,
                        @PathVariable Long groupId,
                        @RequestBody GroupSetRoleRequestDto dto) {
        groupService.setRole(auth, groupId, dto);
    }
}
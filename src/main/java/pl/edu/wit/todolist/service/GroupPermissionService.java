package pl.edu.wit.todolist.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import pl.edu.wit.todolist.entity.GroupEntity;
import pl.edu.wit.todolist.entity.GroupMemberEntity;
import pl.edu.wit.todolist.entity.UserEntity;
import pl.edu.wit.todolist.enums.GroupRole;
import pl.edu.wit.todolist.exception.UserNotFoundException;
import pl.edu.wit.todolist.repository.GroupMemberRepository;
import pl.edu.wit.todolist.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class GroupPermissionService {

    private final UserRepository userRepository;
    private final GroupMemberRepository groupMemberRepository;

    public UserEntity currentUser(Authentication auth) {
        return userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    public GroupMemberEntity requireMember(GroupEntity group, UserEntity user) {
        return groupMemberRepository.findByGroupAndUser(group, user)
                .orElseThrow(() -> new SecurityException("Not a group member"));
    }

    public void requireAdminOrOwner(GroupEntity group, UserEntity user) {
        GroupMemberEntity m = requireMember(group, user);
        if (m.getRole() != GroupRole.ADMIN && m.getRole() != GroupRole.OWNER) {
            throw new SecurityException("Admin/Owner required");
        }
    }

    public void requireOwner(GroupEntity group, UserEntity user) {
        GroupMemberEntity m = requireMember(group, user);
        if (m.getRole() != GroupRole.OWNER) {
            throw new SecurityException("Owner required");
        }
    }
}

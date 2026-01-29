package pl.edu.wit.todolist.service;


import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.wit.todolist.dto.*;
import pl.edu.wit.todolist.entity.GroupEntity;
import pl.edu.wit.todolist.entity.GroupMemberEntity;
import pl.edu.wit.todolist.entity.UserEntity;
import pl.edu.wit.todolist.enums.GroupRole;
import pl.edu.wit.todolist.enums.NotificationType;
import pl.edu.wit.todolist.repository.GroupMemberRepository;
import pl.edu.wit.todolist.repository.GroupRepository;
import pl.edu.wit.todolist.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GroupService {

    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UserRepository userRepository;
    private final GroupPermissionService perm;
    private final NotificationService notificationService;

    private GroupResponseDto toDto(GroupEntity g) {
        return new GroupResponseDto(g.getId(), g.getName(), g.getOwner().getUsername());
    }

    @Transactional
    public GroupResponseDto create(Authentication auth, GroupCreateRequestDto dto) {
        UserEntity owner = perm.currentUser(auth);

        GroupEntity g = groupRepository.save(
                GroupEntity.builder()
                        .name(dto.name())
                        .owner(owner)
                        .build()
        );

        // owner становится участником с ролью OWNER
        groupMemberRepository.save(
                GroupMemberEntity.builder()
                        .group(g)
                        .user(owner)
                        .role(GroupRole.OWNER)
                        .build()
        );

        return toDto(g);
    }

    @Transactional(readOnly = true)
    public List<GroupResponseDto> myGroups(Authentication auth) {
        UserEntity me = perm.currentUser(auth);
        return groupMemberRepository.findAllByUser(me)
                .stream()
                .map(GroupMemberEntity::getGroup)
                .distinct()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public GroupResponseDto rename(Authentication auth, Long groupId, GroupRenameRequestDto dto) {
        GroupEntity g = groupRepository.findById(groupId).orElseThrow();
        UserEntity me = perm.currentUser(auth);
        perm.requireOwner(g, me);

        g.setName(dto.name());
        return toDto(g);
    }

    @Transactional(readOnly = true)
    public List<GroupMemberResponseDto> members(Authentication auth, Long groupId) {
        GroupEntity g = groupRepository.findById(groupId).orElseThrow();
        UserEntity me = perm.currentUser(auth);
        perm.requireMember(g, me);

        return groupMemberRepository.findAllByGroup(g).stream()
                .map(m -> new GroupMemberResponseDto(
                        m.getUser().getId(),
                        m.getUser().getUsername(),
                        m.getUser().getEmail(),
                        m.getRole()
                ))
                .toList();
    }

    // OWNER добавляет участника
    @Transactional
    public void addMember(Authentication auth, Long groupId, String username) {
        GroupEntity g = groupRepository.findById(groupId).orElseThrow();
        UserEntity me = perm.currentUser(auth);
        perm.requireOwner(g, me);

        UserEntity target = userRepository.findByUsername(username).orElseThrow();
        if (groupMemberRepository.existsByGroupAndUser(g, target)) return;

        groupMemberRepository.save(
                GroupMemberEntity.builder()
                        .group(g)
                        .user(target)
                        .role(GroupRole.MEMBER)
                        .build()
        );

        notificationService.notifyUser(
                target,
                NotificationType.GROUP_INVITE_RECEIVED,
                "Added to group",
                "You were added to group: " + g.getName(),
                g.getId(),
                true
        );
    }

    // OWNER даёт/забирает админку
    @Transactional
    public void setRole(Authentication auth, Long groupId, GroupSetRoleRequestDto dto) {
        GroupEntity g = groupRepository.findById(groupId).orElseThrow();
        UserEntity me = perm.currentUser(auth);
        perm.requireOwner(g, me);

        if (dto.role() == null) throw new IllegalArgumentException("Role is required");
        if (dto.role() == GroupRole.OWNER) throw new IllegalArgumentException("Cannot set OWNER role");

        UserEntity target = userRepository.findByUsername(dto.username()).orElseThrow();
        GroupMemberEntity member = groupMemberRepository.findByGroupAndUser(g, target)
                .orElseThrow(() -> new IllegalArgumentException("User not in group"));

        if (member.getRole() == GroupRole.OWNER) {
            throw new IllegalArgumentException("Cannot change OWNER role");
        }

        member.setRole(dto.role());

        // опционально уведомлять
        notificationService.notifyUser(
                target,
                NotificationType.GROUP_INVITE_RECEIVED,
                "Role updated",
                "Your role in group \"" + g.getName() + "\" is now: " + dto.role().name(),
                g.getId(),
                true
        );
    }
}

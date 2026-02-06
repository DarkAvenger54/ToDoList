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
import pl.edu.wit.todolist.repository.*;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GroupService {

    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UserRepository userRepository;
    private final GroupPermissionService perm;
    private final NotificationService notificationService;
    private final TaskRepository taskRepository;
    private final GroupInviteRepository groupInviteRepository;

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

        notificationService.notifyUser(
                target,
                NotificationType.GROUP_INVITE_RECEIVED,
                "Role updated",
                "Your role in group \"" + g.getName() + "\" is now: " + dto.role().name(),
                g.getId(),
                true
        );
    }

    @Transactional
    public void leave(Authentication auth, Long groupId) {
        GroupEntity g = groupRepository.findById(groupId).orElseThrow();
        UserEntity me = perm.currentUser(auth);

        GroupMemberEntity membership = groupMemberRepository
                .findByGroupAndUser(g, me)
                .orElseThrow(() -> new SecurityException("Not a group member"));

        if (membership.getRole() == GroupRole.OWNER) {
            throw new IllegalStateException("Owner cannot leave the group. Delete the group instead.");
        }

        groupMemberRepository.delete(membership);

        UserEntity owner = g.getOwner();
        if (owner != null && !owner.getId().equals(me.getId())) {
            notificationService.notifyUser(
                    owner,
                    NotificationType.GROUP_MEMBER_LEFT,
                    "Member left group",
                    "User " + me.getUsername() + " left group: " + g.getName(),
                    g.getId(),
                    true
            );
        }
    }

    @Transactional
    public void kick(Authentication auth, Long groupId, Long userId) {
        GroupEntity g = groupRepository.findById(groupId).orElseThrow();
        UserEntity me = perm.currentUser(auth);

        perm.requireOwner(g, me);

        UserEntity target = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        GroupMemberEntity targetMembership = groupMemberRepository
                .findByGroupAndUser(g, target)
                .orElseThrow(() -> new IllegalArgumentException("User is not in group"));

        if (targetMembership.getRole() == GroupRole.OWNER) {
            throw new IllegalArgumentException("Cannot kick OWNER");
        }

        groupMemberRepository.delete(targetMembership);

        notificationService.notifyUser(
                target,
                NotificationType.GROUP_MEMBER_KICKED,
                "Removed from group",
                "You were removed from group: " + g.getName(),
                g.getId(),
                true
        );
    }

    @Transactional
    public void deleteGroup(Authentication auth, Long groupId) {
        GroupEntity g = groupRepository.findById(groupId).orElseThrow();
        UserEntity me = perm.currentUser(auth);

        perm.requireOwner(g, me);

        List<UserEntity> members = groupMemberRepository.findAllByGroup(g).stream()
                .map(GroupMemberEntity::getUser)
                .distinct()
                .toList();

        String groupName = g.getName();
        Long gid = g.getId();
        taskRepository.deleteAllByGroup(g);
        groupInviteRepository.deleteAllByGroup(g);
        groupMemberRepository.deleteAllByGroup(g);
        groupRepository.delete(g);
        for (UserEntity u : members) {
            notificationService.notifyUser(
                    u,
                    NotificationType.GROUP_DELETED,
                    "Group deleted",
                    "Group \"" + groupName + "\" was deleted by owner " + me.getUsername(),
                    gid,
                    true
            );
        }
    }
}

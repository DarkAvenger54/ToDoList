package pl.edu.wit.todolist.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.wit.todolist.dto.GroupInviteResponseDto;
import pl.edu.wit.todolist.entity.*;
import pl.edu.wit.todolist.enums.GroupInviteStatus;
import pl.edu.wit.todolist.enums.GroupRole;
import pl.edu.wit.todolist.enums.NotificationType;
import pl.edu.wit.todolist.repository.GroupInviteRepository;
import pl.edu.wit.todolist.repository.GroupMemberRepository;
import pl.edu.wit.todolist.repository.GroupRepository;
import pl.edu.wit.todolist.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GroupInviteService {

    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final GroupInviteRepository inviteRepository;
    private final UserRepository userRepository;
    private final GroupPermissionService perm;
    private final NotificationService notificationService;

    private GroupInviteResponseDto toDto(GroupInviteEntity i) {
        return new GroupInviteResponseDto(
                i.getId(),
                i.getGroup().getId(),
                i.getGroup().getName(),
                i.getInviter().getUsername(),
                i.getStatus(),
                i.getCreatedAt()
        );
    }

    @Transactional
    public void invite(Authentication auth, Long groupId, String username) {
        GroupEntity group = groupRepository.findById(groupId).orElseThrow();
        UserEntity me = perm.currentUser(auth);
        perm.requireOwner(group, me);

        UserEntity invitee = userRepository.findByUsername(username).orElseThrow();

        // если уже участник — молча ничего
        if (groupMemberRepository.existsByGroupAndUser(group, invitee)) return;

        // если уже есть pending invite — молча ничего
        if (inviteRepository.existsByGroupAndInviteeAndStatus(group, invitee, GroupInviteStatus.PENDING)) return;

        // если был старый invite (accepted/rejected) — можно либо создать новый, либо обновить
        inviteRepository.findByGroupAndInvitee(group, invitee).ifPresent(inviteRepository::delete);

        GroupInviteEntity invite = GroupInviteEntity.builder()
                .group(group)
                .inviter(me)
                .invitee(invitee)
                .status(GroupInviteStatus.PENDING)
                .build();

        GroupInviteEntity saved = inviteRepository.save(invite);

        notificationService.notifyUser(
                invitee,
                NotificationType.GROUP_INVITE_RECEIVED,
                "Group invite",
                "You were invited to group: " + group.getName(),
                saved.getId(),
                true
        );
    }

    @Transactional(readOnly = true)
    public List<GroupInviteResponseDto> myPendingInvites(Authentication auth) {
        UserEntity me = perm.currentUser(auth);
        return inviteRepository
                .findAllByInviteeAndStatusOrderByCreatedAtDesc(me, GroupInviteStatus.PENDING)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public void accept(Authentication auth, Long inviteId) {
        UserEntity me = perm.currentUser(auth);

        GroupInviteEntity invite = inviteRepository.findById(inviteId).orElseThrow();
        if (!invite.getInvitee().getId().equals(me.getId())) {
            throw new SecurityException("No access");
        }
        if (invite.getStatus() != GroupInviteStatus.PENDING) {
            throw new IllegalStateException("Invite already processed");
        }

        GroupEntity group = invite.getGroup();

        // создаем membership (если вдруг уже появился)
        if (!groupMemberRepository.existsByGroupAndUser(group, me)) {
            groupMemberRepository.save(
                    GroupMemberEntity.builder()
                            .group(group)
                            .user(me)
                            .role(GroupRole.MEMBER)
                            .build()
            );
        }

        invite.setStatus(GroupInviteStatus.ACCEPTED);
        invite.setRespondedAt(LocalDateTime.now());

        notificationService.notifyUser(
                invite.getInviter(),
                NotificationType.GROUP_INVITE_ACCEPTED,
                "Invite accepted",
                "User " + me.getUsername() + " accepted invite to group: " + group.getName(),
                group.getId(),
                true
        );
    }

    @Transactional
    public void reject(Authentication auth, Long inviteId) {
        UserEntity me = perm.currentUser(auth);

        GroupInviteEntity invite = inviteRepository.findById(inviteId).orElseThrow();
        if (!invite.getInvitee().getId().equals(me.getId())) {
            throw new SecurityException("No access");
        }
        if (invite.getStatus() != GroupInviteStatus.PENDING) {
            throw new IllegalStateException("Invite already processed");
        }

        invite.setStatus(GroupInviteStatus.REJECTED);
        invite.setRespondedAt(LocalDateTime.now());

        notificationService.notifyUser(
                invite.getInviter(),
                NotificationType.GROUP_INVITE_REJECTED,
                "Invite rejected",
                "User " + me.getUsername() + " rejected invite to group: " + invite.getGroup().getName(),
                invite.getGroup().getId(),
                true
        );
    }
}
package pl.edu.wit.todolist.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.wit.todolist.dto.*;
import pl.edu.wit.todolist.entity.*;
import pl.edu.wit.todolist.enums.NotificationType;
import pl.edu.wit.todolist.enums.TaskScope;
import pl.edu.wit.todolist.enums.TaskStatus;
import pl.edu.wit.todolist.repository.*;

@Service
@RequiredArgsConstructor
public class GroupTaskService {

    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;

    private final GroupPermissionService perm;
    private final NotificationService notificationService;

    private TaskResponseDto toDto(TaskEntity t, String currentUsername) {
        String creatorDisplay = (t.getCreator() != null && t.getCreator().getUsername().equals(currentUsername))
                ? "you"
                : (t.getCreator() != null ? t.getCreator().getUsername() : null);

        return new TaskResponseDto(
                t.getId(),
                t.getTitle(),
                t.getDescription(),
                t.getStatus(),
                t.getPriority(),
                t.getScope(),
                t.getDueAt(),
                t.getCreatedAt(),
                t.getUpdatedAt(),
                creatorDisplay,
                t.getGroup() != null ? t.getGroup().getId() : null,
                t.getGroup() != null ? t.getGroup().getName() : null,
                t.getOwner() != null ? t.getOwner().getUsername() : null,
                t.isGroupTask(),
                t.isVisibleInGroup()
        );
    }

    @Transactional
    public TaskResponseDto assignToUser(Authentication auth, Long groupId, GroupAssignTaskRequestDto dto) {
        GroupEntity g = groupRepository.findById(groupId).orElseThrow();
        UserEntity me = perm.currentUser(auth);
        perm.requireAdminOrOwner(g, me);

        UserEntity assignee = userRepository.findByUsername(dto.assigneeUsername()).orElseThrow();
        groupMemberRepository.findByGroupAndUser(g, assignee)
                .orElseThrow(() -> new IllegalArgumentException("Assignee is not in group"));

        TaskEntity t = TaskEntity.builder()
                .creator(me)
                .owner(assignee)
                .group(g)
                .scope(TaskScope.GROUP)
                .groupTask(false)
                .visibleInGroup(dto.visibleInGroup())
                .title(dto.title())
                .description(dto.description())
                .priority(dto.priority())
                .dueAt(dto.dueAt())
                .status(TaskStatus.TODO)
                .build();

        TaskEntity saved = taskRepository.save(t);

        notificationService.notifyUser(
                assignee,
                NotificationType.TASK_ASSIGNED_TO_YOU,
                "New task assigned",
                "Task \"" + saved.getTitle() + "\" was assigned to you in group: " + g.getName(),
                saved.getId(),
                true
        );

        return toDto(saved, me.getUsername());
    }

    @Transactional
    public TaskResponseDto createForAll(Authentication auth, Long groupId, GroupCreateTaskForAllRequestDto dto) {
        GroupEntity g = groupRepository.findById(groupId).orElseThrow();
        UserEntity me = perm.currentUser(auth);
        perm.requireAdminOrOwner(g, me);

        TaskEntity t = TaskEntity.builder()
                .creator(me)
                .owner(null)
                .group(g)
                .scope(TaskScope.GROUP)
                .groupTask(true)
                .visibleInGroup(true)
                .title(dto.title())
                .description(dto.description())
                .priority(dto.priority())
                .dueAt(dto.dueAt())
                .status(TaskStatus.TODO)
                .build();

        TaskEntity saved = taskRepository.save(t);

        for (GroupMemberEntity m : groupMemberRepository.findAllByGroup(g)) {
            notificationService.notifyUser(
                    m.getUser(),
                    NotificationType.TASK_ASSIGNED_TO_YOU,
                    "New group task",
                    "New group task \"" + saved.getTitle() + "\" in group: " + g.getName(),
                    saved.getId(),
                    true
            );
        }

        return toDto(saved, me.getUsername());
    }

    @Transactional
    public void updateGroupTaskStatus(Authentication auth, Long groupId, Long taskId, GroupTaskUpdateStatusRequestDto dto) {
        GroupEntity g = groupRepository.findById(groupId).orElseThrow();
        UserEntity me = perm.currentUser(auth);
        perm.requireAdminOrOwner(g, me);

        TaskEntity task = taskRepository.findById(taskId).orElseThrow();
        if (task.getGroup() == null
                || !task.getGroup().getId().equals(g.getId())
                || !task.isGroupTask()) {
            throw new IllegalArgumentException("Not a group task of this group");
        }

        task.setStatus(dto.status());
    }

    @Transactional(readOnly = true)
    public Page<TaskResponseDto> mine(Authentication auth, Long groupId, Pageable pageable) {
        GroupEntity g = groupRepository.findById(groupId).orElseThrow();
        UserEntity me = perm.currentUser(auth);
        perm.requireMember(g, me);

        Page<TaskEntity> assigned = taskRepository.findAllByGroupAndOwnerAndScopeOrderByCreatedAtDesc(
                g, me, TaskScope.GROUP, pageable
        );

        return assigned.map(t -> toDto(t, me.getUsername()));
    }

    @Transactional(readOnly = true)
    public Page<TaskResponseDto> forAll(Authentication auth, Long groupId, Pageable pageable) {
        GroupEntity g = groupRepository.findById(groupId).orElseThrow();
        UserEntity me = perm.currentUser(auth);
        perm.requireMember(g, me);

        return taskRepository.findAllByGroupAndGroupTaskTrueOrderByCreatedAtDesc(g, pageable)
                .map(t -> toDto(t, me.getUsername()));
    }

    @Transactional(readOnly = true)
    public Page<TaskResponseDto> visible(Authentication auth, Long groupId, Pageable pageable) {
        GroupEntity g = groupRepository.findById(groupId).orElseThrow();
        UserEntity me = perm.currentUser(auth);
        perm.requireMember(g, me);

        return taskRepository.findAllByGroupAndVisibleInGroupTrueOrderByCreatedAtDesc(g, pageable)
                .map(t -> toDto(t, me.getUsername()));
    }
    @Transactional
    public TaskResponseDto updateGroupTask(Authentication auth, Long groupId, Long taskId, GroupTaskUpdateRequestDto dto) {
        GroupEntity g = groupRepository.findById(groupId).orElseThrow();
        UserEntity me = perm.currentUser(auth);
        perm.requireAdminOrOwner(g, me);

        TaskEntity t = taskRepository.findById(taskId).orElseThrow();

        if (t.getGroup() == null
                || !t.getGroup().getId().equals(g.getId())
                || t.getScope() == null
                || t.getScope().name().equals("PERSONAL")) {
            throw new IllegalArgumentException("Not a group task of this group");
        }

        if (dto.title() != null) t.setTitle(dto.title());
        if (dto.description() != null) t.setDescription(dto.description());
        if (dto.status() != null) t.setStatus(dto.status());
        if (dto.priority() != null) t.setPriority(dto.priority());

        if (Boolean.TRUE.equals(dto.clearDueAt())) {
            t.setDueAt(null);
        } else if (dto.dueAt() != null) {
            t.setDueAt(dto.dueAt());
        }

        if (dto.visibleInGroup() != null) {
            t.setVisibleInGroup(dto.visibleInGroup());
        }

        return toDto(t, me.getUsername());
    }

    @Transactional
    public void deleteGroupTask(Authentication auth, Long groupId, Long taskId) {
        GroupEntity g = groupRepository.findById(groupId).orElseThrow();
        UserEntity me = perm.currentUser(auth);
        perm.requireAdminOrOwner(g, me);

        TaskEntity t = taskRepository.findById(taskId).orElseThrow();

        if (t.getGroup() == null
                || !t.getGroup().getId().equals(g.getId())
                || t.getScope() == null
                || t.getScope().name().equals("PERSONAL")) {
            throw new IllegalArgumentException("Not a group task of this group");
        }

        taskRepository.delete(t);
    }
}
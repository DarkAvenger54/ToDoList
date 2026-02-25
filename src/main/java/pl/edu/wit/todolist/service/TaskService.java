package pl.edu.wit.todolist.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.wit.todolist.dto.task.TaskCreateRequestDto;
import pl.edu.wit.todolist.dto.task.TaskResponseDto;
import pl.edu.wit.todolist.dto.task.TaskUpdateRequestDto;
import pl.edu.wit.todolist.entity.TaskEntity;
import pl.edu.wit.todolist.entity.UserEntity;
import pl.edu.wit.todolist.enums.TaskFilter;
import pl.edu.wit.todolist.enums.TaskScope;
import pl.edu.wit.todolist.enums.TaskStatus;
import pl.edu.wit.todolist.repository.TaskRepository;
import pl.edu.wit.todolist.repository.UserRepository;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    private static final List<TaskStatus> UNFINISHED_STATUSES =
            List.of(TaskStatus.TODO, TaskStatus.IN_PROGRESS);

    private UserEntity currentUser(Authentication auth) {
        return userRepository.findByUsername(auth.getName()).orElseThrow();
    }

    private boolean isOverdue(TaskEntity t, Instant now) {
        if (t.getDueAt() == null) return false;
        if (!t.getDueAt().isBefore(now)) return false;
        return t.getStatus() == TaskStatus.TODO || t.getStatus() == TaskStatus.IN_PROGRESS;
    }

    private TaskResponseDto toDto(TaskEntity t, String currentUsername, Instant now) {
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
                t.isVisibleInGroup(),
                isOverdue(t, now)
        );
    }

    @Transactional
    public TaskResponseDto create(Authentication auth, TaskCreateRequestDto dto) {
        UserEntity user = currentUser(auth);
        String currentUsername = auth.getName();

        TaskEntity task = TaskEntity.builder()
                .creator(user)
                .owner(user)
                .title(dto.title())
                .description(dto.description())
                .priority(dto.priority())
                .dueAt(dto.dueAt())
                .scope(TaskScope.PERSONAL)
                .group(null)
                .groupTask(false)
                .visibleInGroup(false)
                .status(TaskStatus.TODO)
                .build();

        return toDto(taskRepository.save(task), currentUsername, Instant.now());
    }

    @Transactional(readOnly = true)
    public Page<TaskResponseDto> list(Authentication auth, TaskFilter filter, TaskStatus status, Pageable pageable) {
        UserEntity user = currentUser(auth);
        String currentUsername = auth.getName();
        Instant now = Instant.now();

        if (filter == null && status != null) {
            return taskRepository.findAllByOwnerAndScopeAndStatus(user, TaskScope.PERSONAL, status, pageable)
                    .map(t -> toDto(t, currentUsername, now));
        }

        TaskFilter resolved = filter != null ? filter : TaskFilter.ALL;
        Page<TaskEntity> page = switch (resolved) {
            case ALL -> taskRepository.findAllByOwnerAndScope(user, TaskScope.PERSONAL, pageable);
            case UNFINISHED -> taskRepository.findAllByOwnerAndScopeAndStatusIn(
                    user,
                    TaskScope.PERSONAL,
                    UNFINISHED_STATUSES,
                    pageable
            );
            case COMPLETED -> taskRepository.findAllByOwnerAndScopeAndStatus(
                    user,
                    TaskScope.PERSONAL,
                    TaskStatus.DONE,
                    pageable
            );
            case OVERDUE -> taskRepository.findAllByOwnerAndScopeAndStatusInAndDueAtIsNotNullAndDueAtBefore(
                    user,
                    TaskScope.PERSONAL,
                    UNFINISHED_STATUSES,
                    now,
                    pageable
            );
        };

        return page.map(t -> toDto(t, currentUsername, now));
    }

    @Transactional(readOnly = true)
    public TaskResponseDto get(Authentication auth, Long id) {
        UserEntity user = currentUser(auth);
        String currentUsername = auth.getName();

        TaskEntity t = taskRepository.findByIdAndOwner(id, user).orElseThrow();
        return toDto(t, currentUsername, Instant.now());
    }

    @Transactional
    public TaskResponseDto update(Authentication auth, Long id, TaskUpdateRequestDto dto) {
        UserEntity user = currentUser(auth);
        String currentUsername = auth.getName();

        TaskEntity t = taskRepository.findByIdAndOwner(id, user).orElseThrow();

        if (dto.title() != null) t.setTitle(dto.title());
        if (dto.description() != null) t.setDescription(dto.description());
        if (dto.status() != null) t.setStatus(dto.status());
        if (dto.priority() != null) t.setPriority(dto.priority());

        if (Boolean.TRUE.equals(dto.clearDueAt())) {
            t.setDueAt(null);
        } else if (dto.dueAt() != null) {
            t.setDueAt(dto.dueAt());
        }

        return toDto(t, currentUsername, Instant.now());
    }

    @Transactional
    public void delete(Authentication auth, Long id) {
        UserEntity user = currentUser(auth);
        TaskEntity t = taskRepository.findByIdAndOwner(id, user).orElseThrow();
        taskRepository.delete(t);
    }
}

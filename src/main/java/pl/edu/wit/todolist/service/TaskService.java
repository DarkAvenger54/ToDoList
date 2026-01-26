package pl.edu.wit.todolist.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.wit.todolist.dto.TaskCreateRequestDto;
import pl.edu.wit.todolist.dto.TaskResponseDto;
import pl.edu.wit.todolist.dto.TaskUpdateRequestDto;
import pl.edu.wit.todolist.entity.TaskEntity;
import pl.edu.wit.todolist.entity.UserEntity;
import pl.edu.wit.todolist.enums.TaskScope;
import pl.edu.wit.todolist.enums.TaskStatus;
import pl.edu.wit.todolist.repository.TaskRepository;
import pl.edu.wit.todolist.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    private UserEntity currentUser(Authentication auth) {
        return userRepository.findByUsername(auth.getName()).orElseThrow();
    }

    private TaskResponseDto toDto(TaskEntity t) {
        return new TaskResponseDto(
                t.getId(),
                t.getTitle(),
                t.getDescription(),
                t.getStatus(),
                t.getPriority(),
                t.getScope(),
                t.getDueAt(),
                t.getCreatedAt(),
                t.getUpdatedAt()
        );
    }

    @Transactional
    public TaskResponseDto create(Authentication auth, TaskCreateRequestDto dto) {
        UserEntity user = currentUser(auth);

        TaskEntity task = TaskEntity.builder()
                .owner(user)
                .title(dto.title())
                .description(dto.description())
                .priority(dto.priority())
                .dueAt(dto.dueAt())
                .scope(TaskScope.PERSONAL)
                .status(TaskStatus.TODO)
                .build();

        return toDto(taskRepository.save(task));
    }

    @Transactional(readOnly = true)
    public Page<TaskResponseDto> list(Authentication auth, TaskStatus status, Pageable pageable) {
        UserEntity user = currentUser(auth);

        Page<TaskEntity> page = (status == null)
                ? taskRepository.findAllByOwnerAndScope(user, TaskScope.PERSONAL, pageable)
                : taskRepository.findAllByOwnerAndScopeAndStatus(user, TaskScope.PERSONAL, status, pageable);

        return page.map(this::toDto);
    }

    @Transactional(readOnly = true)
    public TaskResponseDto get(Authentication auth, Long id) {
        UserEntity user = currentUser(auth);
        TaskEntity t = taskRepository.findByIdAndOwner(id, user).orElseThrow();
        return toDto(t);
    }

    @Transactional
    public TaskResponseDto update(Authentication auth, Long id, TaskUpdateRequestDto dto) {
        UserEntity user = currentUser(auth);
        TaskEntity t = taskRepository.findByIdAndOwner(id, user).orElseThrow();

        if (dto.title() != null) t.setTitle(dto.title());
        if (dto.description() != null) t.setDescription(dto.description());
        if (dto.status() != null) t.setStatus(dto.status());
        if (dto.priority() != null) t.setPriority(dto.priority());
        if (dto.dueAt() != null) t.setDueAt(dto.dueAt());

        // updatedAt выставится в @PreUpdate
        return toDto(t);
    }

    @Transactional
    public void delete(Authentication auth, Long id) {
        UserEntity user = currentUser(auth);
        TaskEntity t = taskRepository.findByIdAndOwner(id, user).orElseThrow();
        taskRepository.delete(t);
    }
}

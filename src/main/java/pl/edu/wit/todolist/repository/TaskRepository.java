package pl.edu.wit.todolist.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import pl.edu.wit.todolist.entity.TaskEntity;
import pl.edu.wit.todolist.entity.UserEntity;
import pl.edu.wit.todolist.enums.TaskScope;
import pl.edu.wit.todolist.enums.TaskStatus;
import pl.edu.wit.todolist.entity.GroupEntity;

import java.util.Optional;


public interface TaskRepository extends JpaRepository<TaskEntity, Long> {

    // PERSONAL
    Page<TaskEntity> findAllByOwnerAndScope(UserEntity owner, TaskScope scope, Pageable pageable);
    Page<TaskEntity> findAllByOwnerAndScopeAndStatus(UserEntity owner, TaskScope scope, TaskStatus status, Pageable pageable);
    Optional<TaskEntity> findByIdAndOwner(Long id, UserEntity owner);

    // GROUP: мои назначенные задачи в группе (owner = assignee)
    Page<TaskEntity> findAllByGroupAndOwnerAndScopeOrderByCreatedAtDesc(GroupEntity group, UserEntity owner, TaskScope scope, Pageable pageable);

    // GROUP: групповая задача для всех (owner=null, groupTask=true)
    Page<TaskEntity> findAllByGroupAndGroupTaskTrueOrderByCreatedAtDesc(GroupEntity group, Pageable pageable);

    // GROUP: лента группы (видимые задачи, которые админ отметил visibleInGroup=true)
    Page<TaskEntity> findAllByGroupAndVisibleInGroupTrueOrderByCreatedAtDesc(GroupEntity group, Pageable pageable);
}
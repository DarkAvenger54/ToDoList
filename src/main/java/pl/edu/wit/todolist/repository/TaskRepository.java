package pl.edu.wit.todolist.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.edu.wit.todolist.entity.TaskEntity;
import pl.edu.wit.todolist.entity.UserEntity;
import pl.edu.wit.todolist.enums.NotificationType;
import pl.edu.wit.todolist.enums.TaskScope;
import pl.edu.wit.todolist.enums.TaskStatus;
import pl.edu.wit.todolist.entity.GroupEntity;

import java.time.LocalDateTime;
import java.util.List;
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

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM TaskEntity t WHERE t.group = :group")
    void deleteAllByGroup(@Param("group") GroupEntity group);

    @Query("""
    SELECT t FROM TaskEntity t
    JOIN FETCH t.owner o
    WHERE o IS NOT NULL
      AND t.dueAt IS NOT NULL
      AND t.status IN ('TODO', 'IN_PROGRESS')
      AND t.dueAt >= :from
      AND t.dueAt <= :to
      AND NOT EXISTS (
          SELECT 1 FROM NotificationEntity n
          WHERE n.user = o
            AND n.type = :type
            AND n.refId = t.id
      )
""")
    List<TaskEntity> findTasksDueSoonNotNotified(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            @Param("type") NotificationType type
    );

}
package pl.edu.wit.todolist.entity;

import jakarta.persistence.*;
import lombok.*;
import pl.edu.wit.todolist.enums.TaskPriority;
import pl.edu.wit.todolist.enums.TaskScope;
import pl.edu.wit.todolist.enums.TaskStatus;

import java.time.LocalDateTime;

@Entity
@Table(name = "tasks", indexes = {
        @Index(name = "idx_tasks_owner", columnList = "owner_id"),
        @Index(name = "idx_tasks_creator", columnList = "creator_id"),
        @Index(name = "idx_tasks_group", columnList = "group_id"),
        @Index(name = "idx_tasks_status", columnList = "status"),
        @Index(name = "idx_tasks_due", columnList = "dueAt"),
        @Index(name = "idx_tasks_visible_group", columnList = "visibleInGroup")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = true)
    private UserEntity owner;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "creator_id", nullable = false)
    private UserEntity creator;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = true)
    private GroupEntity group;

    @Column(nullable = false, length = 140)
    private String title;

    @Column(length = 4000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskPriority priority;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskScope scope;

    @Column
    private LocalDateTime dueAt;

    @Column(nullable = false)
    private boolean groupTask;

    @Column(nullable = false)
    private boolean visibleInGroup;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        if (title != null) title = title.trim();
        if (status == null) status = TaskStatus.TODO;
        if (priority == null) priority = TaskPriority.MEDIUM;
        if (scope == null) scope = TaskScope.PERSONAL;

        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    void preUpdate() {
        if (title != null) title = title.trim();
        updatedAt = LocalDateTime.now();
    }
}

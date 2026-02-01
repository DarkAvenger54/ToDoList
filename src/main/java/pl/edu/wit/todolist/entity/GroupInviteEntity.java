package pl.edu.wit.todolist.entity;

import jakarta.persistence.*;
import lombok.*;
import pl.edu.wit.todolist.enums.GroupInviteStatus;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "group_invites",
        uniqueConstraints = @UniqueConstraint(columnNames = {"group_id", "invitee_id"}),
        indexes = {
                @Index(name = "idx_group_invites_invitee", columnList = "invitee_id"),
                @Index(name = "idx_group_invites_group", columnList = "group_id"),
                @Index(name = "idx_group_invites_status", columnList = "status")
        }
)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class GroupInviteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "group_id", nullable = false)
    private GroupEntity group;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "inviter_id", nullable = false)
    private UserEntity inviter;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "invitee_id", nullable = false)
    private UserEntity invitee;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private GroupInviteStatus status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime respondedAt;

    @PrePersist
    void prePersist() {
        createdAt = LocalDateTime.now();
        if (status == null) status = GroupInviteStatus.PENDING;
    }
}
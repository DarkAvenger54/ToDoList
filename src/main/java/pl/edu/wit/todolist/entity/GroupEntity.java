package pl.edu.wit.todolist.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "groups", indexes = {
        @Index(name = "idx_groups_owner", columnList = "owner_id")
})
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class GroupEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 80)
    private String name;

    // OWNER/создатель один
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private UserEntity owner;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        if (name != null) name = name.trim();
        createdAt = LocalDateTime.now();
    }
}

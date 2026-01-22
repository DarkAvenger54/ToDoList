package pl.edu.wit.todolist.entity;


import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.HashSet;
import java.util.Set;


@Entity
@Table(name = "users")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class UserEntity {
    @Id
    @GeneratedValue
    @EqualsAndHashCode.Include
    private Long id;

    @NotBlank
    @Size(min = 3, max = 50)
    @Column(unique = true, nullable = false)
    private String username;

    @NotBlank
    @Email
    @Column(unique = true, nullable = false)
    private String email;

    @NotBlank
    @Size(min = 6, max = 100)
    @Column(nullable = false)
    private String password;

    @OneToMany(mappedBy = "requester", cascade = CascadeType.REMOVE)
    private Set<FriendshipEntity> sentRequests = new HashSet<>();

    @OneToMany(mappedBy = "addressee", cascade = CascadeType.REMOVE)
    private Set<FriendshipEntity> receivedRequests = new HashSet<>();

    @Column(nullable = false)
    private boolean emailVerified;

    @PrePersist
    @PreUpdate
    private void normalizeFields() {
        if (username != null) {
            username = username.trim();
        }
        if (email != null) {
            email = email.trim().toLowerCase();
        }
    }
}

package pl.edu.wit.todolist.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.edu.wit.todolist.entity.GroupEntity;
import pl.edu.wit.todolist.entity.GroupInviteEntity;
import pl.edu.wit.todolist.entity.UserEntity;
import pl.edu.wit.todolist.enums.GroupInviteStatus;

import java.util.List;
import java.util.Optional;

public interface GroupInviteRepository extends JpaRepository<GroupInviteEntity, Long> {

    Optional<GroupInviteEntity> findByGroupAndInvitee(GroupEntity group, UserEntity invitee);

    List<GroupInviteEntity> findAllByInviteeAndStatusOrderByCreatedAtDesc(UserEntity invitee, GroupInviteStatus status);

    boolean existsByGroupAndInviteeAndStatus(GroupEntity group, UserEntity invitee, GroupInviteStatus status);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM GroupInviteEntity i WHERE i.group = :group")
    void deleteAllByGroup(@Param("group") GroupEntity group);
}
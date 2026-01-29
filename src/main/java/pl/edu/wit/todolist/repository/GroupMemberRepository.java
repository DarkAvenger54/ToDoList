package pl.edu.wit.todolist.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.edu.wit.todolist.entity.GroupEntity;
import pl.edu.wit.todolist.entity.GroupMemberEntity;
import pl.edu.wit.todolist.entity.UserEntity;

import java.util.List;
import java.util.Optional;

public interface GroupMemberRepository extends JpaRepository<GroupMemberEntity, Long> {

    Optional<GroupMemberEntity> findByGroupAndUser(GroupEntity group, UserEntity user);

    List<GroupMemberEntity> findAllByGroup(GroupEntity group);

    List<GroupMemberEntity> findAllByUser(UserEntity user);

    boolean existsByGroupAndUser(GroupEntity group, UserEntity user);
}
package pl.edu.wit.todolist.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.edu.wit.todolist.entity.FriendshipEntity;
import pl.edu.wit.todolist.entity.UserEntity;

import java.util.List;
import java.util.Optional;

public interface FriendshipRepository
        extends JpaRepository<FriendshipEntity, Long> {

    Optional<FriendshipEntity> findByRequesterAndAddressee(
            UserEntity requester,
            UserEntity addressee
    );

    @Query("""
        SELECT f FROM FriendshipEntity f
        WHERE (f.requester = :user OR f.addressee = :user)
          AND f.status = 'ACCEPTED'
    """)
    List<FriendshipEntity> findFriendsOf(@Param("user") UserEntity user);

    @Query("""
        SELECT f FROM FriendshipEntity f
        WHERE f.addressee = :user AND f.status = 'PENDING'
    """)
    List<FriendshipEntity> findIncomingRequests(@Param("user") UserEntity user);
}
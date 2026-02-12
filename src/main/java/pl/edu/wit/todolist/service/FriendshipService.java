package pl.edu.wit.todolist.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.wit.todolist.dto.friend.FriendResponseDto;
import pl.edu.wit.todolist.entity.FriendshipEntity;
import pl.edu.wit.todolist.entity.UserEntity;
import pl.edu.wit.todolist.enums.FriendshipStatus;
import pl.edu.wit.todolist.enums.NotificationType;
import pl.edu.wit.todolist.repository.FriendshipRepository;
import pl.edu.wit.todolist.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class FriendshipService {

    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public void sendRequest(Authentication auth, String targetUsername) {

        UserEntity requester = userRepository
                .findByUsername(auth.getName())
                .orElseThrow();

        UserEntity addressee = userRepository
                .findByUsername(targetUsername)
                .orElseThrow();

        if (requester.equals(addressee)) {
            throw new IllegalArgumentException("Нельзя добавить себя");
        }

        friendshipRepository
                .findByRequesterAndAddressee(requester, addressee)
                .ifPresent(f -> {
                    throw new IllegalStateException("Заявка уже существует");
                });

        FriendshipEntity friendship = FriendshipEntity.builder()
                .requester(requester)
                .addressee(addressee)
                .status(FriendshipStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        friendshipRepository.save(friendship);
        notificationService.notifyUser(
                addressee,
                NotificationType.FRIEND_REQUEST_RECEIVED,
                "Friend request",
                "User " + requester.getUsername() + " sent you a friend request",
                friendship.getId(),
                true
        );
    }

    // ACCEPT REQUEST
    public void accept(Long friendshipId, Authentication auth) {

        FriendshipEntity friendship = friendshipRepository
                .findById(friendshipId)
                .orElseThrow();

        if (!friendship.getAddressee()
                .getUsername()
                .equals(auth.getName())) {
            throw new SecurityException("Нельзя принять чужую заявку");
        }

        if (friendship.getStatus() == FriendshipStatus.ACCEPTED) {
            return;
        }
        if (friendship.getStatus() != FriendshipStatus.PENDING) {
            throw new IllegalArgumentException("Friend request is not pending");
        }

        friendship.setStatus(FriendshipStatus.ACCEPTED);
        UserEntity requester = friendship.getRequester();
        UserEntity addressee = friendship.getAddressee();

        notificationService.notifyUser(
                requester,
                NotificationType.FRIEND_REQUEST_ACCEPTED,
                "Friend request accepted",
                "User " + addressee.getUsername() + " accepted your friend request",
                friendship.getId(),
                true
        );
    }

    // REMOVE FRIEND
    public void remove(Long id, Authentication auth) {

        UserEntity user = userRepository
                .findByUsername(auth.getName())
                .orElseThrow();

        FriendshipEntity friendship = friendshipRepository
                .findById(id)
                .orElse(null);

        if (friendship == null
                || (!friendship.getRequester().equals(user)
                && !friendship.getAddressee().equals(user))) {

            UserEntity other = userRepository
                    .findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Friendship not found"));

            friendship = friendshipRepository
                    .findByRequesterAndAddressee(user, other)
                    .orElseGet(() -> friendshipRepository
                            .findByRequesterAndAddressee(other, user)
                            .orElseThrow(() -> new IllegalArgumentException("Friendship not found")));
        }

        String username = auth.getName();

        if (!friendship.getRequester().getUsername().equals(username)
                && !friendship.getAddressee().getUsername().equals(username)) {
            throw new SecurityException("Нет доступа");
        }

        friendshipRepository.delete(friendship);
    }

    // GET FRIENDS
    @Transactional(readOnly = true)
    public List<FriendResponseDto> friends(Authentication auth) {

        UserEntity user = userRepository
                .findByUsername(auth.getName())
                .orElseThrow();

        return friendshipRepository.findFriendsOf(user)
                .stream()
                .map(f -> {
                    UserEntity friend =
                            f.getRequester().equals(user)
                                    ? f.getAddressee()
                                    : f.getRequester();

                    return new FriendResponseDto(
                            friend.getId(),
                            friend.getUsername(),
                            friend.getEmail()
                    );
                })
                .toList();
    }
}

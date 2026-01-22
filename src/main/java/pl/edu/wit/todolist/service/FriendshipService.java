package pl.edu.wit.todolist.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.wit.todolist.dto.FriendResponseDto;
import pl.edu.wit.todolist.entity.FriendshipEntity;
import pl.edu.wit.todolist.entity.UserEntity;
import pl.edu.wit.todolist.enums.FriendshipStatus;
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

    // SEND REQUEST
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

        friendship.setStatus(FriendshipStatus.ACCEPTED);
    }

    // REMOVE FRIEND
    public void remove(Long friendshipId, Authentication auth) {

        FriendshipEntity friendship = friendshipRepository
                .findById(friendshipId)
                .orElseThrow();

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

package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.UserNotFoundException;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class InMemoryUserServiceImpl implements UserService {
    private final UserStorage userStorage;

    @Override
    public User createUser(User user) {
        log.info("Processing createUser request: {}", user);
        setUsername(user);
        return userStorage.createUser(user);
    }

    @Override
    public User updateUser(User user) {
        log.info("Processing updateUser request for ID: {}", user.getId());

        userStorage.getUserById(user.getId())
                .orElseThrow(() -> {
                    log.error("Cannot update user. User not found with ID: {}", user.getId());
                    return new UserNotFoundException(user.getId());
                });

        setUsername(user);
        return userStorage.updateUser(user);
    }

    @Override
    public List<User> getAllUsers() {
        log.info("Processing getAllUsers request");
        return userStorage.getAllUsers();
    }

    @Override
    public User getUserById(Long id) {
        log.info("Processing getUserById request for ID: {}", id);
        return userStorage.getUserById(id)
                .orElseThrow(() -> {
                    log.error("User not found with ID: {}", id);
                    return new UserNotFoundException(id);
                });
    }

    @Override
    public void addFriend(Long userId, Long friendId) {
        log.info("Processing addFriend request: userId={}, friendId={}", userId, friendId);
        if (userId.equals(friendId)) {
            log.warn("Validation failed: User tried to add themselves. ID={}", userId);
            throw new IllegalArgumentException("User cannot add themselves as a friend. ID: " + userId);
        }

        User user = userStorage.getUserById(userId)
                .orElseThrow(() -> throwUserNotFoundException("User not found with ID: {}", userId));
        User friend = userStorage.getUserById(friendId)
                .orElseThrow(() -> throwUserNotFoundException("Friend not found with ID: {}", friendId));

        user.getFriends().add(friendId);
        friend.getFriends().add(userId);

        log.info("Successfully established friendship between userId={} and friendId={}", userId, friendId);
    }

    @Override
    public void deleteFriend(Long userId, Long friendId) {
        log.info("Processing deleteFriend request: userId={}, friendId={}", userId, friendId);

        if (userId.equals(friendId)) {
            log.warn("Validation failed: User tried to remove themselves from friends. ID={}", userId);
            throw new IllegalArgumentException("User cannot remove themselves from friends. ID: " + userId);
        }

        User user = userStorage.getUserById(userId)
                .orElseThrow(() -> throwUserNotFoundException("User not found with ID: {}", userId));
        User friend = userStorage.getUserById(friendId)
                .orElseThrow(() -> throwUserNotFoundException("Friend not found with ID: {}", friendId));

        user.getFriends().remove(friendId);
        friend.getFriends().remove(userId);

        log.info("Successfully removed friendship between userId={} and friendId={}", userId, friendId);
    }

    @Override
    public List<User> getFriends(Long userId) {
        log.info("Processing getFriends request for userId={}", userId);

        User user = userStorage.getUserById(userId)
                .orElseThrow(() -> {
                    log.error("User not found with ID: {}", userId);
                    return new UserNotFoundException(userId);
                });

        log.debug("Found {} friend ID(s) for userId={}", user.getFriends().size(), userId);

        return user.getFriends().stream()
                .map(userStorage::getUserById)
                .flatMap(Optional::stream)
                .collect(Collectors.toList());
    }

    @Override
    public List<User> getCommonFriends(Long userId, Long otherId) {
        log.info("Processing getCommonFriends request for userId={} and otherId={}", userId, otherId);

        if (userId.equals(otherId)) {
            log.warn("Validation failed: Equal user IDs provided. ID={}", userId);
            throw new IllegalArgumentException("User IDs must be different. ID: " + userId);
        }

        User user = userStorage.getUserById(userId)
                .orElseThrow(() -> throwUserNotFoundException("User not found with ID: {}", userId));
        User other = userStorage.getUserById(otherId)
                .orElseThrow(() -> throwUserNotFoundException("Friend not found with ID: {}", otherId));
        Set<Long> intersect = new HashSet<>(user.getFriends());
        intersect.retainAll(other.getFriends());

        log.debug("Found {} potential common friend ID(s) for userId={} and otherId={}", intersect.size(), userId, otherId);

        return intersect.stream()
                .map(userStorage::getUserById)
                .flatMap(Optional::stream)
                .collect(Collectors.toList());
    }

    private void setUsername(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }

    private static UserNotFoundException throwUserNotFoundException(String message, Long userId) {
        log.error(message, userId);
        return new UserNotFoundException(userId);
    }
}
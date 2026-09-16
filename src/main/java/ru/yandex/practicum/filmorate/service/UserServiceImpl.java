package ru.yandex.practicum.filmorate.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.UserNotFoundException;
import ru.yandex.practicum.filmorate.storage.UserDbStorage;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserServiceImpl implements UserService {
    UserDbStorage userStorage;

    @Override
    public User createUser(User user) {
        log.info("Processing createUser request: {}", user);
        validateName(user);
        return userStorage.createUser(user);
    }

    @Override
    public User updateUser(User user) {
        log.info("Processing updateUser request for ID: {}", user.getId());
        getUserById(user.getId());
        validateName(user);
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
            throw new IllegalArgumentException("User cannot add themselves as a friend. ID: " + userId);
        }

        getUserById(userId);
        getUserById(friendId);

        userStorage.addFriend(userId, friendId);
        log.info("Successfully established friendship between userId={} and friendId={}", userId, friendId);
    }

    @Override
    public void deleteFriend(Long userId, Long friendId) {
        log.info("Processing deleteFriend request: userId={}, friendId={}", userId, friendId);
        if (userId.equals(friendId)) {
            throw new IllegalArgumentException("User cannot remove themselves from friends. ID: " + userId);
        }

        getUserById(userId);
        getUserById(friendId);

        userStorage.deleteFriend(userId, friendId);
        log.info("Successfully removed friendship between userId={} and friendId={}", userId, friendId);
    }

    @Override
    public List<User> getFriends(Long userId) {
        log.info("Processing getFriends request for userId={}", userId);
        getUserById(userId);
        return userStorage.getFriends(userId);
    }

    @Override
    public List<User> getCommonFriends(Long userId, Long otherId) {
        log.info("Processing getCommonFriends request for userId={} and otherId={}", userId, otherId);
        if (userId.equals(otherId)) {
            throw new IllegalArgumentException("User IDs must be different. ID: " + userId);
        }

        getUserById(userId);
        getUserById(otherId);

        return userStorage.getCommonFriends(userId, otherId);
    }

    private void validateName(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }
}

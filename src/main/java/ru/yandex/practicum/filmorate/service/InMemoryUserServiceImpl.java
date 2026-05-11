package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class InMemoryUserServiceImpl implements UserService {
    private final InMemoryUserStorage userStorage;

    @Override
    public User createUser(User user) {
        return userStorage.createUser(user);
    }

    @Override
    public User updateUser(User user) {
        return userStorage.updateUser(user);
    }

    @Override
    public List<User> getAllUsers() {
        return userStorage.getAllUsers();
    }

    @Override
    public User getUserById(Long id){
        return userStorage.getUserById(id);
    }

    @Override
    public void addFriend(Long userId, Long friendId) {
        User user = userStorage.getUserById(userId);
        User friend = userStorage.getUserById(friendId);

        user.getFriends().add(friendId);
        friend.getFriends().add(userId); // Взаимность по условию
    }

    @Override
    public void deleteFriend(Long userId, Long friendId) {
        User user = userStorage.getUserById(userId);
        User friend = userStorage.getUserById(friendId);

        user.getFriends().remove(friendId);
        friend.getFriends().remove(userId);
    }

    @Override
    public List<User> getFriends(Long userId) {
        User user = userStorage.getUserById(userId);
        return user.getFriends().stream()
                .map(userStorage::getUserById)
                .collect(Collectors.toList());
    }

    @Override
    public List<User> getCommonFriends(Long userId, Long otherId) {
        User user = userStorage.getUserById(userId);
        User other = userStorage.getUserById(otherId);

        Set<Long> intersect = new HashSet<>(user.getFriends());
        intersect.retainAll(other.getFriends());

        return intersect.stream()
                .map(userStorage::getUserById)
                .collect(Collectors.toList());
    }

}
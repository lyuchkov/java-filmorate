package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.UserNotFoundException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> userMap = new HashMap<>();
    private long idCounter = 1;

    @Override
    public User createUser(User user) {
        setUsername(user);
        user.setId(idCounter++);
        userMap.put(user.getId(), user);
        return user;
    }

    @Override
    public User updateUser(User user) {
        getUserById(user.getId());
        setUsername(user);
        userMap.put(user.getId(), user);
        return user;
    }

    @Override
    public List<User> getAllUsers() {
        return new ArrayList<>(userMap.values());
    }

    @Override
    public User getUserById(Long id) {
        if (!userMap.containsKey(id)) {
            throw new UserNotFoundException(id);
        }
        return userMap.get(id);
    }

    private static void setUsername(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }
}

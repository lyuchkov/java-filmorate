package ru.yandex.practicum.filmorate.model;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(long id) {
        super("Not found user with id: " + id);
    }
}

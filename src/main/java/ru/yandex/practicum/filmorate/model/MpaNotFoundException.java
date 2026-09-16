package ru.yandex.practicum.filmorate.model;

public class MpaNotFoundException extends RuntimeException {
    public MpaNotFoundException(long id) {
        super("Not found mpa with id: " + id);
    }
}

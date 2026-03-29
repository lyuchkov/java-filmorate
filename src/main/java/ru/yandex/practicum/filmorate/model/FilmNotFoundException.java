package ru.yandex.practicum.filmorate.model;

public class FilmNotFoundException extends RuntimeException {
    public FilmNotFoundException(long id) {
        super("Not found film with id: " + id);
    }
}


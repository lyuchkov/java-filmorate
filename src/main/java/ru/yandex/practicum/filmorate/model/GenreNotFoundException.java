package ru.yandex.practicum.filmorate.model;

import java.util.Arrays;

public class GenreNotFoundException extends RuntimeException {
    public GenreNotFoundException(long... ids) {
        super("Not found genre with ids: " + Arrays.toString(ids));
    }

    public GenreNotFoundException(int... ids) {
        super("Not found genre with ids: " + Arrays.toString(ids));
    }
}

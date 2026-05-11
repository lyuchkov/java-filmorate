package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.FilmNotFoundException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Long, Film> filmMap = new HashMap<>();
    private long idCounter = 1;

    @Override
    public Film addFilm(Film film) {
        film.setId(idCounter++);
        filmMap.put(film.getId(), film);
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        getFilmById(film.getId());
        filmMap.put(film.getId(), film);
        return film;
    }

    @Override
    public List<Film> getAllFilms() {
        return new ArrayList<>(filmMap.values());
    }

    @Override
    public Film getFilmById(Long id) {
        if (!filmMap.containsKey(id)) {
            throw new FilmNotFoundException(id);
        }
        return filmMap.get(id);
    }
}

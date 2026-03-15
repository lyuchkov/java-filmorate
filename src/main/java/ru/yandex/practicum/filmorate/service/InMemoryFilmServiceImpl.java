package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class InMemoryFilmServiceImpl implements FilmService {
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
        if (!filmMap.containsKey(film.getId())) {
            throw new RuntimeException("Film not found");
        }
        filmMap.put(film.getId(), film);
        return film;
    }

    @Override
    public List<Film> getAllFilms() {
        return new ArrayList<>(filmMap.values());
    }
}

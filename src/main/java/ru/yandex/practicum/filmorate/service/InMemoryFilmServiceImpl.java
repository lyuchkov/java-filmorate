package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.FilmNotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class InMemoryFilmServiceImpl implements FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    @Override
    public Film addFilm(Film film) {
        log.info("Processing addFilm request: {}", film);
        Film createdFilm = filmStorage.addFilm(film);
        log.info("Successfully created film with ID: {}", createdFilm.getId());
        return createdFilm;
    }

    @Override
    public Film updateFilm(Film film) {
        log.info("Processing updateFilm request for ID: {}", film.getId());

        filmStorage.getFilmById(film.getId())
                .orElseThrow(() -> {
                    log.error("Cannot update film. Film not found with ID: {}", film.getId());
                    return new FilmNotFoundException( film.getId());
                });

        Film updatedFilm = filmStorage.updateFilm(film);
        log.info("Successfully updated film with ID: {}", updatedFilm.getId());
        return updatedFilm;
    }

    @Override
    public List<Film> getAllFilms() {
        log.info("Processing getAllFilms request");
        List<Film> films = filmStorage.getAllFilms();
        log.debug("Successfully retrieved {} film(s)", films.size());
        return films;
    }

    @Override
    public Film getFilmById(Long id) {
        log.info("Processing getFilmById request for ID: {}", id);

        return filmStorage.getFilmById(id)
                .orElseThrow(() -> {
                    log.error("Film not found with ID: {}", id);
                    return new ru.yandex.practicum.filmorate.model.FilmNotFoundException(id);
                });
    }

    @Override
    public void addLike(Long filmId, Long userId) {
        log.info("Processing addLike request: filmId={}, userId={}", filmId, userId);

        Film film = getFilmById(filmId);

        User user = userStorage.getUserById(userId)
                .orElseThrow(() -> getNoSuchElementException(userId));

        film.getLikes().add(userId);
        log.info("Successfully added like from userId={} to filmId={}", userId, filmId);
    }

    private static NoSuchElementException getNoSuchElementException(Long userId) {
        log.error("User not found with ID: {}", userId);
        return new NoSuchElementException("User with id " + userId + " not found");
    }

    @Override
    public void deleteLike(Long filmId, Long userId) {
        log.info("Processing deleteLike request: filmId={}, userId={}", filmId, userId);

        Film film = getFilmById(filmId);

        userStorage.getUserById(userId)
                .orElseThrow(() -> getNoSuchElementException(userId));

        film.getLikes().remove(userId);
        log.info("Successfully removed like from userId={} for filmId={}", userId, filmId);
    }

    @Override
    public List<Film> getPopular(int count) {
        log.info("Processing getPopular films request with count={}", count);

        if (count <= 0) {
            log.warn("Validation failed: Count must be greater than zero. Received count={}", count);
            throw new IllegalArgumentException("Count must be greater than zero. Count: " + count);
        }

        return filmStorage.getAllFilms().stream()
                .sorted((f1, f2) -> Integer.compare(f2.getLikes().size(), f1.getLikes().size()))
                .limit(count)
                .collect(Collectors.toList());
    }
}
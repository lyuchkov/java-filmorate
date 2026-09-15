package ru.yandex.practicum.filmorate.service;

import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.MpaStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class FilmServiceImpl implements FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final MpaStorage mpaStorage;
    private final GenreService genreService;

    @Override
    public Film addFilm(Film film) {
        log.info("Processing addFilm request: {}", film);
        validateMpa(film);
        validateGenres(film);
        Film createdFilm = filmStorage.addFilm(film);
        log.info("Successfully created film with ID: {}", createdFilm.getId());
        return createdFilm;
    }

    @Override
    public Film updateFilm(Film film) {
        log.info("Processing updateFilm request for ID: {}", film.getId());
        validateMpa(film);
        validateGenres(film);
        filmStorage.getFilmById(film.getId())
                .orElseThrow(() -> {
                    log.error("Cannot update film. Film not found with ID: {}", film.getId());
                    return new FilmNotFoundException(film.getId());
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
                    return new FilmNotFoundException(id);
                });
    }

    @Override
    public void addLike(Long filmId, Long userId) {
        log.info("Processing addLike request: filmId={}, userId={}", filmId, userId);

        getFilmById(filmId);

        userStorage.getUserById(userId)
                .orElseThrow(() -> getUserNotFoundException(userId));

        filmStorage.addLike(filmId, userId);
        log.info("Successfully added like from userId={} to filmId={}", userId, filmId);
    }

    @Override
    public void deleteLike(Long filmId, Long userId) {
        log.info("Processing deleteLike request: filmId={}, userId={}", filmId, userId);

        getFilmById(filmId);

        userStorage.getUserById(userId)
                .orElseThrow(() -> getUserNotFoundException(userId));

        filmStorage.deleteLike(filmId, userId);
        log.info("Successfully removed like from userId={} for filmId={}", userId, filmId);
    }

    @Override
    public List<Film> getPopular(int count) {
        log.info("Processing getPopular films request with count={}", count);

        if (count <= 0) {
            log.warn("Validation failed: Count must be greater than zero. Received count={}", count);
            throw new IllegalArgumentException("Count must be greater than zero. Count: " + count);
        }

        return filmStorage.getPopularFilms(count);
    }

    private static UserNotFoundException getUserNotFoundException(Long userId) {
        log.error("User not found with ID: {}", userId);
        return new UserNotFoundException(userId);
    }

    private void validateMpa(Film film) {
        if (film.getMpa() == null) {
            log.warn("Film validation failed: MPA is required");
            throw new ValidationException("MPA is required");
        }
        int mpaId = film.getMpa().getId();
        this.mpaStorage.getMpaById(mpaId)
                .orElseThrow(() -> {
                    log.warn("MPA not found with ID: {}", mpaId);
                    return new MpaNotFoundException(mpaId);
                });
    }

    private void validateGenres(Film film) {
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            List<Integer> genreIds = film.getGenres().stream()
                    .map(Genre::getId)
                    .distinct()
                    .collect(Collectors.toList());

            this.genreService.getGenres(genreIds);
        }
    }
}
package ru.yandex.practicum.filmorate.service;

import jakarta.validation.ValidationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
class FilmServiceImplTest {

    @Autowired
    private FilmService filmService;

    @Autowired
    private UserStorage userStorage;

    private Film createTestFilm(String name) {
        Mpa mpa = new Mpa();
        mpa.setId(1);

        Genre genre = new Genre();
        genre.setId(1);

        Film film = new Film();
        film.setName(name);
        film.setDescription("Test Description");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(120);
        film.setMpa(mpa);
        film.setGenres(new LinkedHashSet<>(Set.of(genre)));
        return film;
    }

    private User createTestUser(String email, String login) {
        User user = new User();
        user.setEmail(email);
        user.setLogin(login);
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        return userStorage.createUser(user);
    }

    @Test
    void addFilm_shouldCreateAndReturnFilmWithId() {
        Film film = createTestFilm("New Film");
        Film created = filmService.addFilm(film);

        assertThat(created.getId()).isNotNull();
        assertThat(created.getName()).isEqualTo("New Film");
        assertThat(created.getGenres()).hasSize(1);
    }

    @Test
    void addFilm_shouldThrowExceptionWhenMpaIsNull() {
        Film film = createTestFilm("Film Without MPA");
        film.setMpa(null);

        assertThrows(ValidationException.class, () -> filmService.addFilm(film));
    }

    @Test
    void addFilm_shouldThrowExceptionWhenMpaNotFound() {
        Film film = createTestFilm("Film With Invalid MPA");
        Mpa invalidMpa = new Mpa();
        invalidMpa.setId(999);
        film.setMpa(invalidMpa);

        assertThrows(MpaNotFoundException.class, () -> filmService.addFilm(film));
    }

    @Test
    void addFilm_shouldThrowExceptionWhenGenreNotFound() {
        Film film = createTestFilm("Film With Invalid Genre");
        Genre invalidGenre = new Genre();
        invalidGenre.setId(999);
        film.setGenres(new LinkedHashSet<>(Set.of(invalidGenre)));

        assertThrows(GenreNotFoundException.class, () -> filmService.addFilm(film));
    }

    @Test
    void getFilmById_shouldReturnFilmWhenExists() {
        Film created = filmService.addFilm(createTestFilm("Film To Find"));
        Film found = filmService.getFilmById(created.getId());

        assertThat(found).isNotNull();
        assertThat(found.getId()).isEqualTo(created.getId());
    }

    @Test
    void getFilmById_shouldThrowExceptionWhenNotFound() {
        assertThrows(FilmNotFoundException.class, () -> filmService.getFilmById(999L));
    }

    @Test
    void updateFilm_shouldUpdateExistingFilm() {
        Film created = filmService.addFilm(createTestFilm("Original Name"));
        created.setName("Updated Name");

        Film updated = filmService.updateFilm(created);
        assertThat(updated.getName()).isEqualTo("Updated Name");
    }

    @Test
    void updateFilm_shouldThrowExceptionWhenFilmDoesNotExist() {
        Film film = createTestFilm("Non Existing");
        film.setId(999L);

        assertThrows(FilmNotFoundException.class, () -> filmService.updateFilm(film));
    }

    @Test
    void getAllFilms_shouldReturnFilmsList() {
        filmService.addFilm(createTestFilm("Film 1"));
        filmService.addFilm(createTestFilm("Film 2"));

        List<Film> films = filmService.getAllFilms();
        assertThat(films).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    void addAndDeleteLike_shouldAddAndRemoveLikeSuccessfully() {
        Film film = filmService.addFilm(createTestFilm("Liked Film"));
        User user = createTestUser("user@test.com", "userlogin");

        assertDoesNotThrow(() -> filmService.addLike(film.getId(), user.getId()));
        assertDoesNotThrow(() -> filmService.deleteLike(film.getId(), user.getId()));
    }

    @Test
    void getPopular_shouldThrowExceptionWhenCountIsZeroOrNegative() {
        assertThrows(IllegalArgumentException.class, () -> filmService.getPopular(0));
        assertThrows(IllegalArgumentException.class, () -> filmService.getPopular(-1));
    }
}
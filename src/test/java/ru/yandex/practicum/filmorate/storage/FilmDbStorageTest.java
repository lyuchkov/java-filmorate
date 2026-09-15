package ru.yandex.practicum.filmorate.storage;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
class FilmDbStorageTest {

    @Autowired
    private FilmDbStorage filmDbStorage;

    @Autowired
    private UserDbStorage userDbStorage;

    private Film createTestFilm(String name) {
        Mpa mpa = new Mpa();
        mpa.setId(1);

        Genre genre = new Genre();
        genre.setId(1);

        Film film = new Film();
        film.setName(name);
        film.setDescription("Test Desc");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        film.setMpa(mpa);
        film.setGenres(new LinkedHashSet<>(Set.of(genre)));
        return film;
    }

    private User createTestUser() {
        User user = new User();
        user.setEmail("liker@mail.com");
        user.setLogin("liker");
        user.setName("Liker Name");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        return userDbStorage.createUser(user);
    }

    @Test
    void addFilm_shouldSaveAndReturnFilmWithId() {
        Film film = createTestFilm("New Film");
        Film created = filmDbStorage.addFilm(film);

        assertThat(created.getName()).isEqualTo("New Film");
        assertThat(created.getGenres()).hasSize(1);
    }

    @Test
    void updateFilm_shouldUpdateFilmData() {
        Film film = filmDbStorage.addFilm(createTestFilm("Old Name"));
        film.setName("New Name");

        Film updated = filmDbStorage.updateFilm(film);

        assertThat(updated.getName()).isEqualTo("New Name");
        Optional<Film> found = filmDbStorage.getFilmById(film.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("New Name");
    }

    @Test
    void getFilmById_shouldReturnFilmWhenExists() {
        Film film = filmDbStorage.addFilm(createTestFilm("Find Me"));

        Optional<Film> found = filmDbStorage.getFilmById(film.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(film.getId());
    }

    @Test
    void getFilmById_shouldReturnEmptyWhenNotFound() {
        Optional<Film> found = filmDbStorage.getFilmById(999L);
        assertThat(found).isEmpty();
    }

    @Test
    void getAllFilms_shouldReturnAllFilms() {
        filmDbStorage.addFilm(createTestFilm("Film 1"));
        filmDbStorage.addFilm(createTestFilm("Film 2"));

        List<Film> films = filmDbStorage.getAllFilms();

        assertThat(films).hasSizeGreaterThanOrEqualTo(2);
    }

}
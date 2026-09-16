package ru.yandex.practicum.filmorate.storage;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
class GenreDbStorageTest {

    @Autowired
    private GenreDbStorage genreDbStorage;

    @Autowired
    private FilmDbStorage filmDbStorage;

    @Test
    void getAll_shouldReturnAllGenres() {
        List<Genre> genres = genreDbStorage.getAll();
        assertThat(genres).isNotNull().hasSizeGreaterThanOrEqualTo(6);
    }

    @Test
    void getGenreById_shouldReturnGenreWhenExists() {
        Optional<Genre> genre = genreDbStorage.getGenreById(1);
        assertThat(genre).isPresent();
        assertThat(genre.get().getId()).isEqualTo(1);
    }

    @Test
    void getGenreById_shouldReturnEmptyWhenNotFound() {
        Optional<Genre> genre = genreDbStorage.getGenreById(999);
        assertThat(genre).isEmpty();
    }

    @Test
    void getGenresByIds_shouldReturnRequestedGenres() {
        List<Genre> genres = genreDbStorage.getGenresByIds(List.of(1, 2));
        assertThat(genres).hasSize(2);
        assertThat(genres).extracting(Genre::getId).containsExactlyInAnyOrder(1, 2);
    }

    @Test
    void getGenresForFilms_shouldReturnMappedGenres() {
        Mpa mpa = new Mpa();
        mpa.setId(1);

        Genre genre = new Genre();
        genre.setId(1);

        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Test Desc");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(120);
        film.setMpa(mpa);
        film.setGenres(new LinkedHashSet<>(Set.of(genre)));

        Film savedFilm = filmDbStorage.addFilm(film);

        Map<Long, Set<Genre>> map = genreDbStorage.getGenresForFilms(List.of(savedFilm.getId()));

        assertThat(map).isNotEmpty();
        assertThat(map).containsKey(savedFilm.getId());
        assertThat(map.get(savedFilm.getId())).hasSize(1).extracting(Genre::getId).contains(1);
    }
}
package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.GenreNotFoundException;

import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
class GenreServiceTest {

    @Autowired
    private GenreService genreService;

    @Test
    void getAll_shouldReturnAllGenres() {
        Collection<Genre> genres = genreService.getAll();
        assertThat(genres).isNotNull().hasSizeGreaterThanOrEqualTo(6);
    }

    @Test
    void getGenre_shouldReturnGenreWhenIdExists() {
        Genre genre = genreService.getGenre(1);
        assertThat(genre).isNotNull();
        assertThat(genre.getId()).isEqualTo(1);
    }

    @Test
    void getGenre_shouldThrowExceptionWhenIdDoesNotExist() {
        assertThrows(GenreNotFoundException.class, () -> genreService.getGenre(999));
    }

    @Test
    void getGenres_shouldReturnGenresWhenAllIdsExist() {
        List<Genre> genres = genreService.getGenres(List.of(1, 2));
        assertThat(genres).hasSize(2);
    }

    @Test
    void getGenres_shouldThrowExceptionWhenAnyIdDoesNotExist() {
        assertThrows(GenreNotFoundException.class, () -> genreService.getGenres(List.of(1, 999)));
    }
}
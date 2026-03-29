package ru.yandex.practicum.filmorate.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FilmValidationTest {

    private final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();

    private LocalDate createDate(int year, int month, int day) {
        return LocalDate.of(year, month, day);
    }

    private Film createValidFilm() {
        Film film = new Film();
        film.setName("Valid Name");
        film.setDescription("Valid Description");
        film.setReleaseDate(createDate(2000, 1, 1));
        film.setDuration(120);
        return film;
    }

    @Test
    void shouldPassValidationWhenFilmIsValid() {
        Film film = createValidFilm();

        Validator validator = factory.getValidator();
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertTrue(violations.isEmpty());
    }

    @Test
    void shouldFailWhenNameIsBlank() {
        Film film = createValidFilm();
        film.setName("   ");

        Validator validator = factory.getValidator();
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertEquals(1, violations.size());
    }

    @Test
    void shouldPassWhenDescriptionIsExactly200Chars() {
        Film film = createValidFilm();
        film.setDescription("a".repeat(200));


        Validator validator = factory.getValidator();
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertTrue(violations.isEmpty());
    }

    @Test
    void shouldFailWhenDescriptionIs201Chars() {
        Film film = createValidFilm();
        film.setDescription("a".repeat(201));


        Validator validator = factory.getValidator();
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertEquals(1, violations.size());
    }

    @Test
    void shouldPassWhenReleaseDateIsAfter1895_12_28() {
        Film film = createValidFilm();
        film.setReleaseDate(createDate(1895, 12, 29));


        Validator validator = factory.getValidator();
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertTrue(violations.isEmpty());
    }

    @Test
    void shouldNotFailWhenReleaseDateIsExactly1895_12_28() {
        Film film = createValidFilm();
        film.setReleaseDate(createDate(1895, 12, 28));

        Validator validator = factory.getValidator();
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertEquals(1, violations.size());
    }

    @Test
    void shouldFailWhenReleaseDateIsBefore1895_12_28() {
        Film film = createValidFilm();
        film.setReleaseDate(createDate(1895, 12, 27));

        Validator validator = factory.getValidator();
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertEquals(1, violations.size());
    }

    @Test
    void shouldPassWhenDurationIs1() {
        Film film = createValidFilm();
        film.setDuration(1);


        Validator validator = factory.getValidator();
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertTrue(violations.isEmpty());
    }

    @Test
    void shouldFailWhenDurationIs0() {
        Film film = createValidFilm();
        film.setDuration(0);


        Validator validator = factory.getValidator();
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertEquals(1, violations.size());
    }

    @Test
    void shouldFailWhenDurationIsNegative() {
        Film film = createValidFilm();
        film.setDuration(-1);


        Validator validator = factory.getValidator();
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertEquals(1, violations.size());
    }
}
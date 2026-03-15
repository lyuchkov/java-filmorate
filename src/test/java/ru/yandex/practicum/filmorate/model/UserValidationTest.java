package ru.yandex.practicum.filmorate.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UserValidationTest {

    private final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();

    private User createValidUser() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("validLogin");
        user.setBirthday(Date.from(Instant.now().minus(1, ChronoUnit.DAYS)));
        return user;
    }

    @Test
    void shouldPassValidationWhenUserIsValid() {
        User user = createValidUser();

        Set<ConstraintViolation<User>> violations =  factory.getValidator().validate(user);
        assertTrue(violations.isEmpty());
    }

    @Test
    void shouldFailWhenEmailIsInvalid() {
        User user = createValidUser();
        user.setEmail("invalid-email");

        Set<ConstraintViolation<User>> violations =  factory.getValidator().validate(user);
        assertEquals(1, violations.size());
    }

    @Test
    void shouldFailWhenLoginContainsSpaces() {
        User user = createValidUser();
        user.setLogin("invalid login");

        Set<ConstraintViolation<User>> violations =  factory.getValidator().validate(user);
        assertEquals(1, violations.size());
    }

    @Test
    void shouldFailWhenLoginIsBlank() {
        User user = createValidUser();
        user.setLogin("   ");

        Set<ConstraintViolation<User>> violations =  factory.getValidator().validate(user);
        assertEquals(2, violations.size());
    }

    @Test
    void shouldFailWhenBirthdayIsInFuture() {
        User user = createValidUser();
        user.setBirthday(Date.from(Instant.now().plus(1, ChronoUnit.DAYS)));

        Set<ConstraintViolation<User>> violations =  factory.getValidator().validate(user);
        assertEquals(1, violations.size());
    }

    @Test
    void shouldFailWhenBirthdayIsNow() {
        User user = createValidUser();
        user.setBirthday(Date.from(Instant.now().plusMillis(5000)));

        Set<ConstraintViolation<User>> violations =  factory.getValidator().validate(user);
        assertEquals(1, violations.size());
    }
}
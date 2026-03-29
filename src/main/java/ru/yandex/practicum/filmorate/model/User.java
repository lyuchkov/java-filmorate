package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class User {
    private static final String NO_SPACES_REGEX = "^\\S*$";

    long id;

    @Email
    @NotBlank
    String email;

    @NotBlank
    @Pattern(regexp = NO_SPACES_REGEX, message = "Field must not contain any spaces")
    String login;

    String name;

    @Past
    @NotNull
    LocalDate birthday;
}

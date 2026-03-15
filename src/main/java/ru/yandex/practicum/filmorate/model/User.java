package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Data
public class User {
    private static final String NO_SPACES_REGEX = "^\\S*$";

    long id;

    @Email
    String email;

    @NotBlank
    @Pattern(regexp = NO_SPACES_REGEX, message = "Field must not contain any spaces")
    String login;

    String name;

    @Past
    Date birthday;
}

package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import ru.yandex.practicum.filmorate.model.validation.DateOnlyAfter;

import java.time.LocalDate;

@Data
public class Film {
    long id;

    @NotBlank
    String name;

    @Size(max = 200, message = "Film description length should be less than 200")
    String description;

    @DateOnlyAfter(value = "1895-12-28")
    LocalDate releaseDate;

    @Positive
    long duration;

}

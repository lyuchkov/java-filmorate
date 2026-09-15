package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.MpaNotFoundException;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
class MpaServiceImplTest {

    @Autowired
    private MpaService mpaService;

    @Test
    void getAllMpa_shouldReturnAllMpaRatings() {
        Collection<Mpa> mpaList = mpaService.getAllMpa();
        assertThat(mpaList).isNotNull().hasSizeGreaterThanOrEqualTo(5);
    }

    @Test
    void getMpa_shouldReturnMpaWhenIdExists() {
        Mpa mpa = mpaService.getMpa(1);
        assertThat(mpa).isNotNull();
        assertThat(mpa.getId()).isEqualTo(1);
    }

    @Test
    void getMpa_shouldThrowExceptionWhenIdDoesNotExist() {
        assertThrows(MpaNotFoundException.class, () -> mpaService.getMpa(999));
    }
}
package ru.yandex.practicum.filmorate.storage;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
class MpaDbStorageTest {

    @Autowired
    private MpaDbStorage mpaDbStorage;

    @Test
    void getAll_shouldReturnAllMpa() {
        List<Mpa> mpaList = mpaDbStorage.getAll();
        assertThat(mpaList).isNotNull().hasSizeGreaterThanOrEqualTo(5);
    }

    @Test
    void getMpaById_shouldReturnMpaWhenExists() {
        Optional<Mpa> mpa = mpaDbStorage.getMpaById(1);
        assertThat(mpa).isPresent();
        assertThat(mpa.get().getId()).isEqualTo(1);
    }

    @Test
    void getMpaById_shouldReturnEmptyWhenNotFound() {
        Optional<Mpa> mpa = mpaDbStorage.getMpaById(999);
        assertThat(mpa).isEmpty();
    }
}
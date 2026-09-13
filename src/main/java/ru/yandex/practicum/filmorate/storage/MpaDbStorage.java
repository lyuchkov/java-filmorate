package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.mapper.MpaRowMapper;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MpaDbStorage implements MpaStorage {

    private static final String SELECT_ALL_MPA = "SELECT id, name FROM filmorate.mpa ORDER BY id";

    private static final String SELECT_MPA_BY_ID = "SELECT id, name FROM filmorate.mpa WHERE id = ?";

    private final JdbcTemplate jdbcTemplate;
    private final MpaRowMapper mpaRowMapper;

    @Override
    public List<Mpa> getAll() {
        return this.jdbcTemplate.query(SELECT_ALL_MPA, this.mpaRowMapper);
    }

    @Override
    public Optional<Mpa> getMpaById(int id) {
        return this.jdbcTemplate.query(SELECT_MPA_BY_ID, this.mpaRowMapper, id).stream().findFirst();
    }
}

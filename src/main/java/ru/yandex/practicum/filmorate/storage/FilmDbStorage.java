package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.mapper.FilmRowMapper;
import ru.yandex.practicum.filmorate.storage.mapper.GenreRowMapper;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;


@Slf4j
@Repository
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;
    private final FilmRowMapper filmRowMapper;

    private static final String INSERT_FILM = "INSERT INTO FILMORATE.FILMS (name, description, release_date, duration, mpa_id) VALUES (?, ?, ?, ?, ?)";

    private static final String SELECT_FILM_WITH_MPA = "SELECT f.id, f.name, f.description, f.release_date, f.duration, " + "f.mpa_id, m.id as mpa_id_from_join, m.name as mpa_name " + "FROM FILMORATE.FILMS f " + "LEFT JOIN FILMORATE.MPA m ON f.mpa_id = m.id";

    private static final String SELECT_FILM_WITH_MPA_BY_ID = SELECT_FILM_WITH_MPA + " WHERE f.id = ?";

    private static final String UPDATE_FILM = "UPDATE FILMORATE.FILMS SET name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ? WHERE id = ?";

    private static final String DELETE_FILM_GENRES = "DELETE FROM FILMORATE.FILM_GENRES WHERE film_id = ?";

    private static final String SELECT_ALL_FILMS_WITH_MPA = SELECT_FILM_WITH_MPA + " ORDER BY f.id";

    private static final String INSERT_FILM_GENRE = "MERGE INTO FILMORATE.film_genres KEY (film_id, genre_id) VALUES (?, ?)";



    @Override
    public Film addFilm(Film film) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        this.jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(INSERT_FILM, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, Date.valueOf(film.getReleaseDate()));
            ps.setLong(4, film.getDuration());
            if (film.getMpa() != null) {
                ps.setInt(5, film.getMpa().getId());
            } else {
                ps.setNull(5, java.sql.Types.INTEGER);
            }
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key != null) {
            film.setId(key.longValue());
        }

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            addGenresToFilm(film);
        }

        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        this.jdbcTemplate.update(UPDATE_FILM, film.getName(), film.getDescription(), Date.valueOf(film.getReleaseDate()), film.getDuration(), film.getMpa() != null ? film.getMpa().getId() : null, film.getId());

        this.jdbcTemplate.update(DELETE_FILM_GENRES, film.getId());

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            addGenresToFilm(film);
        }

        return film;
    }

    @Override
    public List<Film> getAllFilms() {
        return this.jdbcTemplate.query(SELECT_ALL_FILMS_WITH_MPA, (rs, rowNum) -> {
            Film f = this.filmRowMapper.mapRow(rs, rowNum);
            loadMpaFromResultSet(rs, f);
            return f;
        });
    }

    @Override
    public Optional<Film> getFilmById(Long id) {
        return this.jdbcTemplate.query(SELECT_FILM_WITH_MPA_BY_ID, (rs, rowNum) -> {
            Film f = this.filmRowMapper.mapRow(rs, rowNum);
            loadMpaFromResultSet(rs, f);
            loadGenresFromResultSet(f);
            return f;
        }, id).stream().findFirst();
    }

    private void loadMpaFromResultSet(java.sql.ResultSet rs, Film film) throws java.sql.SQLException {
        if (rs.getObject("mpa_id_from_join") != null) {
            Mpa mpa = new Mpa();
            mpa.setId(rs.getInt("mpa_id_from_join"));
            mpa.setName(rs.getString("mpa_name"));
            film.setMpa(mpa);
        }
    }
    private void loadGenresFromResultSet(Film film) {
        if (film == null) {
            return;
        }

        String sql = "SELECT g.id AS genre_id, g.name AS genre_name " +
                "FROM filmorate.film_genres fg " +
                "JOIN filmorate.genres g ON fg.genre_id = g.id " +
                "WHERE fg.film_id = ? " +
                "ORDER BY g.id";

        List<Genre> genres = jdbcTemplate.query(sql, (rs, rowNum) -> {
            Genre genre = new Genre();
            genre.setId(rs.getInt("genre_id"));
            genre.setName(rs.getString("genre_name"));
            return genre;
        }, film.getId());

        film.setGenres(new LinkedHashSet<>(genres));
    }

    private void addGenresToFilm(Film film) {
        List<Object[]> batchArgs = film.getGenres().stream()
                .map(genre -> new Object[]{film.getId(), genre.getId()})
                .collect(Collectors.toList());

        this.jdbcTemplate.batchUpdate(INSERT_FILM_GENRE, batchArgs);
    }
}

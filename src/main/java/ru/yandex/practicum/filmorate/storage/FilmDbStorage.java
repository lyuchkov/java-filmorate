package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.mapper.FilmRowMapper;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Repository
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;
    private final FilmRowMapper filmRowMapper;

    private static final String INSERT_FILM = "INSERT INTO FILMORATE.FILMS (name, description, release_date, duration, mpa_id) VALUES (?, ?, ?, ?, ?)";

    private static final String SELECT_FILM_WITH_MPA = "SELECT f.id, f.name, f.description, f.release_date, f.duration, " +
            "f.mpa_id, m.id as mpa_id_from_join, m.name as mpa_name " +
            "FROM FILMORATE.FILMS f " +
            "LEFT JOIN FILMORATE.MPA m ON f.mpa_id = m.id";

    private static final String SELECT_FILM_WITH_MPA_BY_ID = SELECT_FILM_WITH_MPA + " WHERE f.id = ?";

    private static final String UPDATE_FILM = "UPDATE FILMORATE.FILMS SET name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ? WHERE id = ?";

    private static final String DELETE_FILM_GENRES = "DELETE FROM FILMORATE.FILM_GENRES WHERE film_id = ?";

    private static final String SELECT_ALL_FILMS_WITH_MPA = SELECT_FILM_WITH_MPA + " ORDER BY f.id";

    private static final String INSERT_FILM_GENRE = "MERGE INTO FILMORATE.FILM_GENRES KEY (film_id, genre_id) VALUES (?, ?)";

    private static final String INSERT_FILM_LIKE = "MERGE INTO FILMORATE.FILM_LIKES KEY (film_id, user_id) VALUES (?, ?)";

    private static final String DELETE_FILM_LIKE = "DELETE FROM FILMORATE.FILM_LIKES WHERE film_id = ? AND user_id = ?";

    private static final String SELECT_POPULAR_FILMS = "SELECT f.id, f.name, f.description, f.release_date, f.duration, " +
            "f.mpa_id, m.id as mpa_id_from_join, m.name as mpa_name " +
            "FROM FILMORATE.FILMS f " +
            "LEFT JOIN FILMORATE.MPA m ON f.mpa_id = m.id " +
            "LEFT JOIN FILMORATE.FILM_LIKES fl ON f.id = fl.film_id " +
            "GROUP BY f.id, m.id, m.name " +
            "ORDER BY COUNT(fl.user_id) DESC, f.id ASC " +
            "LIMIT ?";

    private static final String GET_ALL_FILMS = "SELECT fg.film_id, g.id AS genre_id, g.name AS genre_name " +
            "FROM FILMORATE.FILM_GENRES fg " +
            "JOIN FILMORATE.GENRES g ON fg.genre_id = g.id " +
            "ORDER BY g.id";

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
        } else if (film.getGenres() == null) {
            film.setGenres(new LinkedHashSet<>());
        }

        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        this.jdbcTemplate.update(UPDATE_FILM,
                film.getName(),
                film.getDescription(),
                Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                film.getMpa() != null ? film.getMpa().getId() : null,
                film.getId());

        // Удаляем старые связи с жанрами
        this.jdbcTemplate.update(DELETE_FILM_GENRES, film.getId());

        // Записываем новые связи
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            addGenresToFilm(film);
        } else if (film.getGenres() == null) {
            film.setGenres(new LinkedHashSet<>()); // Чтобы в ответе отдавать [], а не null
        }

        return film;
    }

    @Override
    public List<Film> getAllFilms() {
        List<Film> films = this.jdbcTemplate.query(SELECT_ALL_FILMS_WITH_MPA, (rs, rowNum) -> {
            Film f = this.filmRowMapper.mapRow(rs, rowNum);
            loadMpaFromResultSet(rs, f);
            return f;
        });

        enrichFilmsWithGenres(films);
        return films;
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

    @Override
    public void addLike(Long filmId, Long userId) {
        this.jdbcTemplate.update(INSERT_FILM_LIKE, filmId, userId);
    }

    @Override
    public void deleteLike(Long filmId, Long userId) {
        this.jdbcTemplate.update(DELETE_FILM_LIKE, filmId, userId);
    }

    @Override
    public List<Film> getPopularFilms(int count) {
        List<Film> films = this.jdbcTemplate.query(SELECT_POPULAR_FILMS, (rs, rowNum) -> {
            Film f = this.filmRowMapper.mapRow(rs, rowNum);
            loadMpaFromResultSet(rs, f);
            return f;
        }, count);

        enrichFilmsWithGenres(films);
        return films;
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
                "FROM FILMORATE.FILM_GENRES fg " +
                "JOIN FILMORATE.GENRES g ON fg.genre_id = g.id " +
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

    private void enrichFilmsWithGenres(List<Film> films) {
        if (films == null || films.isEmpty()) {
            return;
        }

        Map<Long, Set<Genre>> filmGenresMap = jdbcTemplate.query(GET_ALL_FILMS, rs -> {
            Map<Long, Set<Genre>> map = new HashMap<>();
            while (rs.next()) {
                Long filmId = rs.getLong("film_id");
                Genre genre = new Genre();
                genre.setId(rs.getInt("genre_id"));
                genre.setName(rs.getString("genre_name"));

                map.computeIfAbsent(filmId, k -> new LinkedHashSet<>()).add(genre);
            }
            return map;
        });

        for (Film film : films) {
            film.setGenres(filmGenresMap != null
                    ? filmGenresMap.getOrDefault(film.getId(), new LinkedHashSet<>())
                    : new LinkedHashSet<>());
        }
    }
}
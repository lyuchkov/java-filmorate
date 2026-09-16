package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.mapper.UserRowMapper;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {

    private final JdbcTemplate jdbcTemplate;
    private final UserRowMapper userRowMapper;

    private static final String INSERT_USER = "INSERT INTO filmorate.users (email, login, name, birthday) VALUES (?, ?, ?, ?)";

    private static final String INSERT_FRIENDSHIP = "INSERT INTO filmorate.friendships (user_id, friend_id) VALUES (?, ?)";

    private static final String UPDATE_USER = "UPDATE filmorate.users SET email = ?, login = ?, name = ?, birthday = ? WHERE id = ?";

    private static final String DELETE_USER = "DELETE FROM filmorate.users WHERE id = ?";

    private static final String DELETE_FRIENDSHIP = "DELETE FROM filmorate.friendships WHERE user_id = ? AND friend_id = ?";

    private static final String SELECT_USER_BY_ID = "SELECT id, email, login, name, birthday FROM filmorate.users WHERE id = ?";

    private static final String SELECT_ALL_USERS = "SELECT id, email, login, name, birthday FROM filmorate.users";

    private static final String SELECT_FRIENDS = "SELECT u.id, u.email, u.login, u.name, u.birthday " + "FROM filmorate.users u " + "INNER JOIN filmorate.friendships f ON u.id = f.friend_id " + "WHERE f.user_id = ?";

    private static final String SELECT_COMMON_FRIENDS = "SELECT u.id, u.email, u.login, u.name, u.birthday " + "FROM filmorate.users u " + "INNER JOIN filmorate.friendships f1 ON u.id = f1.friend_id " + "INNER JOIN filmorate.friendships f2 ON u.id = f2.friend_id " + "WHERE f1.user_id = ? AND f2.user_id = ?";


    @Override
    public User createUser(User user) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        this.jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(INSERT_USER, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getLogin());
            ps.setString(3, user.getName());
            ps.setDate(4, Date.valueOf(user.getBirthday()));
            return ps;
        }, keyHolder);

        user.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());
        return user;
    }

    @Override
    public User updateUser(User user) {
        this.jdbcTemplate.update(UPDATE_USER, user.getEmail(), user.getLogin(), user.getName(), Date.valueOf(user.getBirthday()), user.getId());
        return user;
    }

    @Override
    public List<User> getAllUsers() {
        return this.jdbcTemplate.query(SELECT_ALL_USERS, this.userRowMapper);
    }

    @Override
    public Optional<User> getUserById(Long id) {
        return this.jdbcTemplate.query(SELECT_USER_BY_ID, this.userRowMapper, id).stream().findFirst();
    }

    @Override
    public void addFriend(Long userId, Long friendId) {
        this.jdbcTemplate.update(INSERT_FRIENDSHIP, userId, friendId);
    }

    @Override
    public void deleteFriend(Long userId, Long friendId) {
        this.jdbcTemplate.update(DELETE_FRIENDSHIP, userId, friendId);
    }

    @Override
    public List<User> getFriends(Long userId) {
        return this.jdbcTemplate.query(SELECT_FRIENDS, this.userRowMapper, userId);
    }

    @Override
    public List<User> getCommonFriends(Long userId, Long otherId) {
        return this.jdbcTemplate.query(SELECT_COMMON_FRIENDS, this.userRowMapper, userId, otherId);
    }
}

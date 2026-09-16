package ru.yandex.practicum.filmorate.storage;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
class UserDbStorageTest {

    @Autowired
    private UserDbStorage userDbStorage;

    private User createTestUser(String email, String login) {
        User user = new User();
        user.setEmail(email);
        user.setLogin(login);
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        return userDbStorage.createUser(user);
    }

    @Test
    void createUser_shouldSaveAndReturnUserWithId() {
        User user = new User();
        user.setEmail("test@mail.com");
        user.setLogin("testlogin");
        user.setName("Test Name");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        User created = userDbStorage.createUser(user);

        assertThat(created.getId()).isNotNull();
        assertThat(created.getEmail()).isEqualTo("test@mail.com");
    }

    @Test
    void updateUser_shouldUpdateUserData() {
        User user = createTestUser("old@mail.com", "oldlogin");
        user.setName("New Name");

        User updated = userDbStorage.updateUser(user);

        assertThat(updated.getName()).isEqualTo("New Name");
        Optional<User> found = userDbStorage.getUserById(user.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("New Name");
    }

    @Test
    void getUserById_shouldReturnUserWhenExists() {
        User user = createTestUser("find@mail.com", "findme");

        Optional<User> found = userDbStorage.getUserById(user.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(user.getId());
    }

    @Test
    void getUserById_shouldReturnEmptyWhenNotFound() {
        Optional<User> found = userDbStorage.getUserById(999L);
        assertThat(found).isEmpty();
    }

    @Test
    void getAllUsers_shouldReturnAllUsers() {
        createTestUser("u1@mail.com", "u1");
        createTestUser("u2@mail.com", "u2");

        List<User> users = userDbStorage.getAllUsers();

        assertThat(users).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    void addFriendAndGetFriends_shouldWorkCorrectly() {
        User user1 = createTestUser("f1@mail.com", "f1");
        User user2 = createTestUser("f2@mail.com", "f2");

        userDbStorage.addFriend(user1.getId(), user2.getId());

        List<User> friends = userDbStorage.getFriends(user1.getId());
        assertThat(friends).hasSize(1).extracting(User::getId).contains(user2.getId());
    }

    @Test
    void deleteFriend_shouldRemoveFriend() {
        User user1 = createTestUser("d1@mail.com", "d1");
        User user2 = createTestUser("d2@mail.com", "d2");

        userDbStorage.addFriend(user1.getId(), user2.getId());
        userDbStorage.deleteFriend(user1.getId(), user2.getId());

        List<User> friends = userDbStorage.getFriends(user1.getId());
        assertThat(friends).isEmpty();
    }

    @Test
    void getCommonFriends_shouldReturnIntersection() {
        User user1 = createTestUser("c1@mail.com", "c1");
        User user2 = createTestUser("c2@mail.com", "c2");
        User common = createTestUser("com@mail.com", "com");

        userDbStorage.addFriend(user1.getId(), common.getId());
        userDbStorage.addFriend(user2.getId(), common.getId());

        List<User> commonFriends = userDbStorage.getCommonFriends(user1.getId(), user2.getId());

        assertThat(commonFriends).hasSize(1).extracting(User::getId).containsExactly(common.getId());
    }
}
package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.UserNotFoundException;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
class UserServiceImplTest {

    @Autowired
    private UserService userService;

    private User createTestUser(String email, String login, String name) {
        User user = new User();
        user.setEmail(email);
        user.setLogin(login);
        user.setName(name);
        user.setBirthday(LocalDate.of(1990, 1, 1));
        return user;
    }

    @Test
    void createUser_shouldCreateUserAndUseLoginWhenNameIsEmpty() {
        User user = createTestUser("test1@mail.com", "testlogin", "");
        User created = userService.createUser(user);

        assertThat(created.getId()).isNotNull();
        assertThat(created.getName()).isEqualTo("testlogin");
    }

    @Test
    void createUser_shouldCreateUserWithProvidedName() {
        User user = createTestUser("test2@mail.com", "login2", "Real Name");
        User created = userService.createUser(user);

        assertThat(created.getId()).isNotNull();
        assertThat(created.getName()).isEqualTo("Real Name");
    }

    @Test
    void getUserById_shouldReturnUserWhenExists() {
        User user = userService.createUser(createTestUser("find@mail.com", "findlogin", "Find Me"));
        User found = userService.getUserById(user.getId());

        assertThat(found).isNotNull();
        assertThat(found.getId()).isEqualTo(user.getId());
    }

    @Test
    void getUserById_shouldThrowExceptionWhenUserDoesNotExist() {
        assertThrows(UserNotFoundException.class, () -> userService.getUserById(999L));
    }

    @Test
    void updateUser_shouldUpdateUser() {
        User user = userService.createUser(createTestUser("orig@mail.com", "origlogin", "Old Name"));
        user.setName("New Name");

        User updated = userService.updateUser(user);
        assertThat(updated.getName()).isEqualTo("New Name");
    }

    @Test
    void updateUser_shouldThrowExceptionWhenUserDoesNotExist() {
        User user = createTestUser("nonexist@mail.com", "nonexist", "Name");
        user.setId(999L);

        assertThrows(UserNotFoundException.class, () -> userService.updateUser(user));
    }

    @Test
    void getAllUsers_shouldReturnList() {
        userService.createUser(createTestUser("u1@mail.com", "u1", "User 1"));
        userService.createUser(createTestUser("u2@mail.com", "u2", "User 2"));

        List<User> users = userService.getAllUsers();
        assertThat(users).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    void addFriend_shouldAddFriendSuccessfully() {
        User user1 = userService.createUser(createTestUser("f1@mail.com", "f1", "Friend 1"));
        User user2 = userService.createUser(createTestUser("f2@mail.com", "f2", "Friend 2"));

        assertDoesNotThrow(() -> userService.addFriend(user1.getId(), user2.getId()));

        List<User> friends = userService.getFriends(user1.getId());
        assertThat(friends).extracting(User::getId).contains(user2.getId());
    }

    @Test
    void addFriend_shouldThrowExceptionWhenAddingSelfAsFriend() {
        User user = userService.createUser(createTestUser("self@mail.com", "self", "Self"));

        assertThrows(IllegalArgumentException.class, () -> userService.addFriend(user.getId(), user.getId()));
    }

    @Test
    void deleteFriend_shouldRemoveFriendSuccessfully() {
        User user1 = userService.createUser(createTestUser("df1@mail.com", "df1", "User 1"));
        User user2 = userService.createUser(createTestUser("df2@mail.com", "df2", "User 2"));

        userService.addFriend(user1.getId(), user2.getId());
        userService.deleteFriend(user1.getId(), user2.getId());

        List<User> friends = userService.getFriends(user1.getId());
        assertThat(friends).extracting(User::getId).doesNotContain(user2.getId());
    }

    @Test
    void deleteFriend_shouldThrowExceptionWhenRemovingSelf() {
        User user = userService.createUser(createTestUser("dself@mail.com", "dself", "Self"));

        assertThrows(IllegalArgumentException.class, () -> userService.deleteFriend(user.getId(), user.getId()));
    }

    @Test
    void getCommonFriends_shouldReturnIntersectionOfFriends() {
        User user1 = userService.createUser(createTestUser("c1@mail.com", "c1", "User 1"));
        User user2 = userService.createUser(createTestUser("c2@mail.com", "c2", "User 2"));
        User common = userService.createUser(createTestUser("com@mail.com", "com", "Common Friend"));

        userService.addFriend(user1.getId(), common.getId());
        userService.addFriend(user2.getId(), common.getId());

        List<User> commonFriends = userService.getCommonFriends(user1.getId(), user2.getId());

        assertThat(commonFriends).extracting(User::getId).containsExactly(common.getId());
    }

    @Test
    void getCommonFriends_shouldThrowExceptionWhenIdsAreSame() {
        User user = userService.createUser(createTestUser("same@mail.com", "same", "Same User"));

        assertThrows(IllegalArgumentException.class, () -> userService.getCommonFriends(user.getId(), user.getId()));
    }
}
package ru.practicum.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import ru.practicum.shareit.dto.UserDto;
import ru.practicum.shareit.server.ShareItServer;
import ru.practicum.shareit.server.exception.NotFoundException;
import ru.practicum.shareit.server.user.User;
import ru.practicum.shareit.server.user.UserRepository;
import ru.practicum.shareit.server.user.UserService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(classes = ShareItServer.class)
class UserServiceImplIntegrationTest {
    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void cleanDb() {
        userRepository.deleteAll();
    }

    @Test
    void create_shouldSaveUserToDatabase() {
        UserDto dto = UserDto.builder()
                .name("John")
                .email("john@mail.com")
                .build();

        UserDto created = userService.create(dto);

        assertThat(created.getId()).isNotNull();
        assertThat(userRepository.findAll()).hasSize(1);
    }

    @Test
    void create_shouldThrowIfEmailExists() {
        userRepository.save(User.builder()
                .name("Existing")
                .email("exist@mail.com")
                .build());

        UserDto dto = UserDto.builder()
                .name("New")
                .email("exist@mail.com")
                .build();

        assertThatThrownBy(() -> userService.create(dto))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void getById_shouldReturnUser() {
        User saved = userRepository.save(User.builder()
                .name("Mike")
                .email("mike@mail.com")
                .build());

        UserDto found = userService.getById(saved.getId());

        assertThat(found.getEmail()).isEqualTo("mike@mail.com");
    }

    @Test
    void getById_shouldThrowIfNotFound() {
        assertThatThrownBy(() -> userService.getById(999L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getAll_shouldReturnAllUsers() {
        userRepository.save(User.builder().name("A").email("a@mail.com").build());
        userRepository.save(User.builder().name("B").email("b@mail.com").build());
        List<UserDto> users = userService.getAll();
        assertThat(users).hasSize(2);
    }

    @Test
    void update_shouldChangeNameAndEmail() {
        User saved = userRepository.save(User.builder()
                .name("Old")
                .email("old@mail.com")
                .build());

        UserDto updateDto = UserDto.builder()
                .name("New")
                .email("new@mail.com")
                .build();
        UserDto updated = userService.update(saved.getId(), updateDto);
        assertThat(updated.getName()).isEqualTo("New");
        assertThat(updated.getEmail()).isEqualTo("new@mail.com");
    }

    @Test
    void update_shouldThrowIfEmailTaken() {
        userRepository.save(User.builder()
                .name("User1")
                .email("user1@mail.com")
                .build());
        User second = userRepository.save(User.builder()
                .name("User2")
                .email("user2@mail.com")
                .build());
        UserDto dto = UserDto.builder()
                .email("user1@mail.com")
                .build();
        assertThatThrownBy(() -> userService.update(second.getId(), dto))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void delete_shouldRemoveUserFromDatabase() {
        User saved = userRepository.save(User.builder()
                .name("Delete")
                .email("delete@mail.com")
                .build());
        userService.delete(saved.getId());
        assertThat(userRepository.findById(saved.getId())).isEmpty();
    }
}
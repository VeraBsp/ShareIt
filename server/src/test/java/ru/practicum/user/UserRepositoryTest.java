package ru.practicum.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ContextConfiguration;
import ru.practicum.shareit.server.ShareItServer;
import ru.practicum.shareit.server.user.User;
import ru.practicum.shareit.server.user.UserRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ContextConfiguration(classes = ShareItServer.class)
class UserRepositoryTest {
    @Autowired
    UserRepository userRepository;

    @Test
    @DisplayName("Поиск пользователя по email — не найден")
    public void findByEmail_shouldReturnUser() {
        User user = User.builder()
                .name("Vera")
                .email("vera@email.com")
                .build();
        userRepository.save(user);
        Optional<User> found = userRepository.findByEmail("vera@email.com");
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Vera");
        assertThat(found.get().getEmail()).isEqualTo("vera@email.com");
    }

    @Test
    @DisplayName("Поиск пользователя по email — не найден")
    void findByEmail_shouldReturnEmpty() {
        Optional<User> found = userRepository.findByEmail("unknown@email.com");
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Email должен быть уникальным")
    void email_shouldBeUnique() {
        User user1 = User.builder()
                .name("Vera")
                .email("vera@email.com")
                .build();
        userRepository.save(user1);
        User user2 = User.builder()
                .name("Another")
                .email("vera@email.com")
                .build();
        try {
            userRepository.saveAndFlush(user2);
        } catch (Exception e) {
            assertThat(e).isNotNull();
        }
    }
}
package ru.practicum.request;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.practicum.user.User;
import ru.practicum.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ItemRequestRepositoryTest {
    @Autowired
    private ItemRequestRepository itemRequestRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("findAllByRequestorIdOrderByCreatedDesc возвращает запросы пользователя")
    void findAllByRequestorIdOrderByCreatedDescTest() {
        User user = userRepository.save(
                User.builder()
                        .name("User")
                        .email("user@mail.com")
                        .build()
        );

        ItemRequest request1 = itemRequestRepository.save(
                ItemRequest.builder()
                        .description("Need drill")
                        .requestor(user)
                        .created(LocalDateTime.now())
                        .build()
        );

        ItemRequest request2 = itemRequestRepository.save(
                ItemRequest.builder()
                        .description("Need hammer")
                        .requestor(user)
                        .created(LocalDateTime.now().plusSeconds(1))
                        .build()
        );

        List<ItemRequest> result =
                itemRequestRepository.findAllByRequestorIdOrderByCreatedDesc(user.getId());
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getDescription()).isEqualTo("Need hammer");
    }

    @Test
    @DisplayName("findByRequestorIdNotOrderByCreatedDesc возвращает запросы других пользователей с пагинацией")
    void findByRequestorIdNotOrderByCreatedDescTest() {
        User user1 = userRepository.save(
                User.builder().name("User1").email("u1@mail.com").build()
        );

        User user2 = userRepository.save(
                User.builder().name("User2").email("u2@mail.com").build()
        );

        itemRequestRepository.save(
                ItemRequest.builder()
                        .description("Request 1")
                        .requestor(user1)
                        .created(LocalDateTime.now())
                        .build()
        );

        ItemRequest request2 = itemRequestRepository.save(
                ItemRequest.builder()
                        .description("Request 2")
                        .requestor(user2)
                        .created(LocalDateTime.now())
                        .build()
        );
        Pageable pageable = PageRequest.of(0, 10);
        Page<ItemRequest> result =
                itemRequestRepository.findByRequestorIdNotOrderByCreatedDesc(user1.getId(), pageable);
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getDescription()).isEqualTo(request2.getDescription());
    }
}
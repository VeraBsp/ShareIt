package ru.practicum.item;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ContextConfiguration;
import ru.practicum.shareit.server.ShareItServer;
import ru.practicum.shareit.server.item.Item;
import ru.practicum.shareit.server.item.ItemRepository;
import ru.practicum.shareit.server.request.ItemRequest;
import ru.practicum.shareit.server.request.ItemRequestRepository;
import ru.practicum.shareit.server.user.User;
import ru.practicum.shareit.server.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ContextConfiguration(classes = ShareItServer.class)
class ItemRepositoryTest {
    @Autowired
    ItemRepository itemRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ItemRequestRepository itemRequestRepository;

    @Test
    @DisplayName("findByOwnerId должен возвращать вещи владельца")
    void findByOwnerIdTest() {
        User owner = userRepository.save(
                User.builder().name("Owner").email("owner@mail.com").build()
        );

        Item item1 = itemRepository.save(
                Item.builder()
                        .name("Drill")
                        .description("Power drill")
                        .available(true)
                        .owner(owner)
                        .build()
        );

        Item item2 = itemRepository.save(
                Item.builder()
                        .name("Hammer")
                        .description("Steel hammer")
                        .available(true)
                        .owner(owner)
                        .build()
        );

        Page<Item> result = itemRepository.findByOwnerId(owner.getId(), PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).containsExactlyInAnyOrder(item1, item2);
    }

    @Test
    @DisplayName("search должен находить только доступные вещи по тексту")
    void searchAvailableItemsTest() {
        User owner = userRepository.save(
                User.builder().name("Owner").email("search@mail.com").build()
        );

        itemRepository.save(
                Item.builder()
                        .name("Bike")
                        .description("Mountain bike")
                        .available(true)
                        .owner(owner)
                        .build()
        );

        itemRepository.save(
                Item.builder()
                        .name("Bike broken")
                        .description("Old bike")
                        .available(false)
                        .owner(owner)
                        .build()
        );
        Page<Item> result = itemRepository.search("bike", PageRequest.of(0, 10));
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Bike");
    }

    @Test
    @DisplayName("existsByOwner_Id должен возвращать true если у владельца есть вещи")
    void existsByOwnerIdTest() {
        User owner = userRepository.save(
                User.builder().name("Owner").email("exists@mail.com").build()
        );

        itemRepository.save(
                Item.builder()
                        .name("Saw")
                        .description("Hand saw")
                        .available(true)
                        .owner(owner)
                        .build()
        );

        boolean exists = itemRepository.existsByOwner_Id(owner.getId());
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("findByRequestId должен возвращать вещи по requestId")
    void findByRequestIdTest() {
        User owner = userRepository.save(
                User.builder().name("Owner").email("request@mail.com").build()
        );

        ItemRequest request = itemRequestRepository.save(
                ItemRequest.builder()
                        .description("Need tent")
                        .requestor(owner)
                        .created(LocalDateTime.now())
                        .build()
        );

        Item item = itemRepository.save(
                Item.builder()
                        .name("Tent")
                        .description("Camping tent")
                        .available(true)
                        .owner(owner)
                        .request(request)
                        .build()
        );
        List<Item> result = itemRepository.findByRequest_Id(request.getId());
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getRequest().getId()).isEqualTo(request.getId());
    }
}
package ru.practicum.item;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ContextConfiguration;
import ru.practicum.shareit.server.ShareItServer;
import ru.practicum.shareit.server.item.Comment;
import ru.practicum.shareit.server.item.CommentRepository;
import ru.practicum.shareit.server.item.Item;
import ru.practicum.shareit.server.item.ItemRepository;
import ru.practicum.shareit.server.user.User;
import ru.practicum.shareit.server.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ContextConfiguration(classes = ShareItServer.class)
class CommentRepositoryTest {
    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void findByItemIdOrderByCreatedDesc_shouldReturnCommentsInCorrectOrder() {
        User user = userRepository.save(
                User.builder()
                        .name("User")
                        .email("user@mail.com")
                        .build()
        );

        Item item = itemRepository.save(
                Item.builder()
                        .name("Item")
                        .description("Desc")
                        .available(true)
                        .owner(user)
                        .build()
        );

        Comment first = commentRepository.save(
                Comment.builder()
                        .text("First comment")
                        .item(item)
                        .author(user)
                        .created(LocalDateTime.now().minusMinutes(10))
                        .build()
        );

        Comment second = commentRepository.save(
                Comment.builder()
                        .text("Second comment")
                        .item(item)
                        .author(user)
                        .created(LocalDateTime.now())
                        .build()
        );

        List<Comment> result = commentRepository
                .findByItemIdOrderByCreatedDesc(item.getId());

        assertThat(result).hasSize(2);

        assertThat(result.get(0).getText())
                .isEqualTo("Second comment");

        assertThat(result.get(1).getText())
                .isEqualTo("First comment");
    }
}
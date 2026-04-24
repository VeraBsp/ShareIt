package ru.practicum.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.booking.Booking;
import ru.practicum.booking.BookingRepository;
import ru.practicum.booking.BookingStatus;
import ru.practicum.exception.BadRequestException;
import ru.practicum.exception.ForbiddenException;
import ru.practicum.request.ItemRequest;
import ru.practicum.request.ItemRequestRepository;
import ru.practicum.user.User;
import ru.practicum.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ItemServiceImplIntegrationTest {
    @Autowired
    private ItemService itemService;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private ItemRequestRepository requestRepository;

    private User owner;
    private User booker;

    @BeforeEach
    void setup() {
        bookingRepository.deleteAll();
        commentRepository.deleteAll();
        itemRepository.deleteAll();
        requestRepository.deleteAll();
        userRepository.deleteAll();

        owner = userRepository.save(User.builder()
                .name("Owner")
                .email("owner@mail.com")
                .build());

        booker = userRepository.save(User.builder()
                .name("Booker")
                .email("booker@mail.com")
                .build());
    }

    @Test
    void create_shouldSaveItemWithRequest() {
        ItemRequest request = requestRepository.save(ItemRequest.builder()
                .description("Need item")
                .requestor(owner)
                .created(LocalDateTime.now())
                .build());
        ItemDto dto = ItemDto.builder()
                .name("Drill")
                .description("Power drill")
                .available(true)
                .requestId(request.getId())
                .build();
        ItemDto created = itemService.create(dto, owner.getId());
        assertThat(created.getId()).isNotNull();
        assertThat(itemRepository.findAll()).hasSize(1);
    }

    @Test
    void update_shouldChangeFields() {
        Item item = itemRepository.save(Item.builder()
                .name("Old")
                .description("Old desc")
                .available(true)
                .owner(owner)
                .build());

        ItemDto update = ItemDto.builder()
                .name("New")
                .description("New desc")
                .available(false)
                .build();
        ItemDto updated = itemService.update(item.getId(), update, owner.getId());
        assertThat(updated.getName()).isEqualTo("New");
        assertThat(updated.getAvailable()).isFalse();
    }

    @Test
    void update_shouldThrowIfNotOwner() {
        Item item = itemRepository.save(Item.builder()
                .name("Item")
                .description("Desc")
                .available(true)
                .owner(owner)
                .build());

        assertThatThrownBy(() ->
                itemService.update(item.getId(),
                        ItemDto.builder().name("Hack").build(),
                        booker.getId()))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void getById_shouldReturnItemWithComments() {
        Item item = itemRepository.save(Item.builder()
                .name("Item")
                .description("Desc")
                .available(true)
                .owner(owner)
                .build());

        commentRepository.save(Comment.builder()
                .text("Good")
                .item(item)
                .author(booker)
                .created(LocalDateTime.now())
                .build());

        ItemWithBookingDto dto = itemService.getById(item.getId(), booker.getId());
        assertThat(dto.getComments()).hasSize(1);
    }

    @Test
    void search_shouldFindItemByText() {
        itemRepository.save(Item.builder()
                .name("Laptop")
                .description("Gaming laptop")
                .available(true)
                .owner(owner)
                .build());

        List<ItemDto> result = itemService.search("laptop", 0, 10);

        assertThat(result).hasSize(1);
    }

    @Test
    void addComment_shouldSaveCommentIfBookingExists() {
        Item item = itemRepository.save(Item.builder()
                .name("Item")
                .description("Desc")
                .available(true)
                .owner(owner)
                .build());

        bookingRepository.save(Booking.builder()
                .item(item)
                .booker(booker)
                .start(LocalDateTime.now().minusDays(2))
                .end(LocalDateTime.now().minusDays(1))
                .status(BookingStatus.APPROVED)
                .build());

        CommentDto comment = CommentDto.builder()
                .text("Nice item")
                .build();

        CommentDto saved = itemService.addComment(item.getId(), booker.getId(), comment);

        assertThat(saved.getId()).isNotNull();
        assertThat(commentRepository.findAll()).hasSize(1);
    }

    @Test
    void addComment_shouldThrowIfNoBooking() {
        Item item = itemRepository.save(Item.builder()
                .name("Item")
                .description("Desc")
                .available(true)
                .owner(owner)
                .build());

        assertThatThrownBy(() ->
                itemService.addComment(item.getId(), booker.getId(),
                        CommentDto.builder().text("Fail").build()))
                .isInstanceOf(BadRequestException.class);
    }


}
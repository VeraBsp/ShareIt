package ru.practicum.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import ru.practicum.booking.Booking;
import ru.practicum.booking.BookingRepository;
import ru.practicum.booking.BookingStatus;
import ru.practicum.exception.BadRequestException;
import ru.practicum.exception.ForbiddenException;
import ru.practicum.request.ItemRequest;
import ru.practicum.request.ItemRequestRepository;
import ru.practicum.user.User;
import ru.practicum.user.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {
    @Mock
    ItemRepository itemRepository;
    @Mock
    UserRepository userRepository;
    @Mock CommentRepository commentRepository;
    @Mock
    BookingRepository bookingRepository;
    @Mock
    ItemRequestRepository requestRepository;

    ItemService itemService;

    @BeforeEach
    void setUp() {
        itemService = new ItemServiceImpl(
                itemRepository,
                userRepository,
                commentRepository,
                bookingRepository,
                requestRepository
        );
    }

    @Test
    void create_shouldSaveItemWithoutRequest() {
        User owner = User.builder().id(1L).name("Owner").build();

        ItemDto dto = ItemDto.builder()
                .name("Drill")
                .description("Power drill")
                .available(true)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(itemRepository.save(any(Item.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        ItemDto result = itemService.create(dto, 1L);

        assertThat(result.getName()).isEqualTo("Drill");
        verify(itemRepository).save(any(Item.class));
    }

    @Test
    void create_shouldAttachRequest() {
        User owner = User.builder().id(1L).build();
        ItemRequest request = ItemRequest.builder().id(10L).build();

        ItemDto dto = ItemDto.builder()
                .name("Drill")
                .description("Power drill")
                .available(true)
                .requestId(10L)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(requestRepository.findById(10L)).thenReturn(Optional.of(request));
        when(itemRepository.save(any(Item.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        ItemDto result = itemService.create(dto, 1L);

        assertThat(result.getRequestId()).isEqualTo(10L);
    }

    // ---------- UPDATE ----------

    @Test
    void update_shouldUpdateFields() {
        User owner = User.builder().id(1L).build();

        Item item = Item.builder()
                .id(5L)
                .name("Old")
                .description("Old desc")
                .available(true)
                .owner(owner)
                .build();

        ItemDto dto = ItemDto.builder()
                .name("New")
                .description("New desc")
                .available(false)
                .build();

        when(itemRepository.findById(5L)).thenReturn(Optional.of(item));
        when(itemRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ItemDto result = itemService.update(5L, dto, 1L);

        assertThat(result.getName()).isEqualTo("New");
        assertThat(result.getAvailable()).isFalse();
    }

    @Test
    void update_shouldThrowIfNotOwner() {
        User owner = User.builder().id(1L).build();

        Item item = Item.builder()
                .id(5L)
                .owner(owner)
                .build();

        when(itemRepository.findById(5L)).thenReturn(Optional.of(item));

        assertThrows(ForbiddenException.class,
                () -> itemService.update(5L, new ItemDto(), 2L));
    }

    @Test
    void getById_shouldSetBookingsForOwner() {
        User owner = User.builder().id(1L).build();

        Item item = Item.builder()
                .id(5L)
                .name("Item")
                .owner(owner)
                .available(true)
                .description("Desc")
                .build();

        Booking last = Booking.builder()
                .id(100L)
                .booker(User.builder().id(2L).build())
                .build();

        when(itemRepository.findById(5L)).thenReturn(Optional.of(item));
        when(commentRepository.findByItemIdOrderByCreatedDesc(5L))
                .thenReturn(List.of());
        when(bookingRepository.findLastBooking(eq(5L), any()))
                .thenReturn(Optional.of(last));
        when(bookingRepository.findNextBooking(eq(5L), any()))
                .thenReturn(Optional.empty());

        ItemWithBookingDto dto = itemService.getById(5L, 1L);

        assertThat(dto.getLastBooking()).isNotNull();
        assertThat(dto.getNextBooking()).isNull();
    }

    @Test
    void getById_shouldNotSetBookingsForNotOwner() {
        User owner = User.builder().id(1L).build();

        Item item = Item.builder()
                .id(5L)
                .owner(owner)
                .available(true)
                .description("Desc")
                .build();

        when(itemRepository.findById(5L)).thenReturn(Optional.of(item));
        when(commentRepository.findByItemIdOrderByCreatedDesc(5L))
                .thenReturn(List.of());

        ItemWithBookingDto dto = itemService.getById(5L, 2L);

        assertThat(dto.getLastBooking()).isNull();
        verify(bookingRepository, never()).findLastBooking(any(), any());
    }

    @Test
    void search_shouldReturnEmptyIfTextBlank() {
        List<ItemDto> result = itemService.search("   ", 0, 10);
        assertThat(result).isEmpty();
        verify(itemRepository, never()).search(any(), any());
    }

    @Test
    void getAllByOwner_shouldReturnItems() {
        User owner = User.builder().id(1L).build();

        Item item = Item.builder()
                .id(1L)
                .name("Item")
                .description("Desc")
                .available(true)
                .owner(owner)
                .build();

        when(itemRepository.findByOwnerId(eq(1L), any()))
                .thenReturn(new PageImpl<>(List.of(item)));

        when(commentRepository.findByItemIdOrderByCreatedDesc(1L))
                .thenReturn(List.of());

        List<ItemWithBookingDto> result =
                itemService.getAllByOwner(1L, 0, 10);

        assertThat(result).hasSize(1);
    }

    @Test
    void addComment_shouldSaveCommentIfUserHadBooking() {
        User user = User.builder().id(1L).name("User").build();
        Item item = Item.builder().id(5L).build();

        CommentDto dto = CommentDto.builder().text("Great").build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(itemRepository.findById(5L)).thenReturn(Optional.of(item));
        when(bookingRepository
                .existsByBooker_IdAndItem_IdAndEndBeforeAndStatus(
                        eq(1L), eq(5L), any(), eq(BookingStatus.APPROVED)))
                .thenReturn(true);

        when(commentRepository.save(any()))
                .thenAnswer(inv -> {
                    Comment c = inv.getArgument(0);
                    c.setId(99L);
                    return c;
                });

        CommentDto result = itemService.addComment(5L, 1L, dto);

        assertThat(result.getText()).isEqualTo("Great");
        assertThat(result.getAuthorName()).isEqualTo("User");
    }

    @Test
    void addComment_shouldThrowIfNoBooking() {
        when(userRepository.findById(1L))
                .thenReturn(Optional.of(User.builder().id(1L).build()));
        when(itemRepository.findById(5L))
                .thenReturn(Optional.of(Item.builder().id(5L).build()));
        when(bookingRepository
                .existsByBooker_IdAndItem_IdAndEndBeforeAndStatus(
                        any(), any(), any(), any()))
                .thenReturn(false);

        assertThrows(BadRequestException.class,
                () -> itemService.addComment(5L, 1L,
                        CommentDto.builder().text("Bad").build()));
    }
}
package ru.practicum.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import ru.practicum.shareit.server.booking.*;
import ru.practicum.shareit.server.exception.BadRequestException;
import ru.practicum.shareit.server.exception.ForbiddenException;
import ru.practicum.shareit.server.exception.NotFoundException;
import ru.practicum.shareit.server.item.Item;
import ru.practicum.shareit.server.item.ItemRepository;
import ru.practicum.shareit.server.user.User;
import ru.practicum.shareit.server.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @Mock
    BookingRepository bookingRepository;
    @Mock
    UserRepository userRepository;
    @Mock
    ItemRepository itemRepository;

    BookingService bookingService;

    @BeforeEach
    void setUp() {
        bookingService = new BookingServiceImpl(
                bookingRepository,
                userRepository,
                itemRepository
        );
    }

    @Test
    void create_shouldCreateBooking() {
        Long userId = 1L;

        User booker = User.builder().id(userId).build();
        User owner = User.builder().id(2L).build();

        Item item = Item.builder()
                .id(10L)
                .available(true)
                .owner(owner)
                .name("Drill")
                .build();

        BookingCreateDto dto = BookingCreateDto.builder()
                .itemId(10L)
                .start(LocalDateTime.now().plusHours(1))
                .end(LocalDateTime.now().plusHours(2))
                .build();

        Booking saved = Booking.builder()
                .id(100L)
                .item(item)
                .booker(booker)
                .start(dto.getStart())
                .end(dto.getEnd())
                .status(BookingStatus.WAITING)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));
        when(bookingRepository.save(any())).thenReturn(saved);

        BookingDto result = bookingService.create(userId, dto);

        assertThat(result.getId()).isEqualTo(100L);
        verify(bookingRepository).save(any());
    }

    @Test
    void create_shouldThrowIfItemNotAvailable() {
        User user = User.builder().id(1L).build();
        Item item = Item.builder().id(10L).available(false).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));

        assertThrows(BadRequestException.class,
                () -> bookingService.create(1L,
                        BookingCreateDto.builder()
                                .itemId(10L)
                                .start(LocalDateTime.now().plusHours(1))
                                .end(LocalDateTime.now().plusHours(2))
                                .build()));
    }

    @Test
    void create_shouldThrowIfOwnerBooksOwnItem() {
        User owner = User.builder().id(1L).build();
        Item item = Item.builder()
                .id(10L)
                .available(true)
                .owner(owner)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));

        assertThrows(NotFoundException.class,
                () -> bookingService.create(1L,
                        BookingCreateDto.builder()
                                .itemId(10L)
                                .start(LocalDateTime.now().plusHours(1))
                                .end(LocalDateTime.now().plusHours(2))
                                .build()));
    }

    @Test
    void approve_shouldApproveBooking() {
        User owner = User.builder().id(2L).build();
        User booker = User.builder().id(1L).build();

        Item item = Item.builder()
                .id(10L)
                .name("Drill")
                .owner(owner)
                .build();

        Booking booking = Booking.builder()
                .id(5L)
                .status(BookingStatus.WAITING)
                .item(item)
                .booker(booker)
                .build();

        when(bookingRepository.findById(5L))
                .thenReturn(Optional.of(booking));
        when(bookingRepository.save(any()))
                .thenReturn(booking);

        BookingDto result = bookingService.approve(2L, 5L, true);

        assertThat(result.getStatus()).isEqualTo(BookingStatus.APPROVED);
        verify(bookingRepository).save(booking);
    }

    @Test
    void approve_shouldThrowIfNotOwner() {
        Booking booking = Booking.builder()
                .id(5L)
                .status(BookingStatus.WAITING)
                .item(Item.builder().owner(User.builder().id(2L).build()).build())
                .build();

        when(bookingRepository.findById(5L)).thenReturn(Optional.of(booking));

        assertThrows(NotFoundException.class,
                () -> bookingService.approve(1L, 5L, true));
    }

    @Test
    void getById_shouldReturnIfBooker() {
        User booker = User.builder().id(1L).build();

        Booking booking = Booking.builder()
                .id(3L)
                .booker(booker)
                .item(Item.builder().owner(User.builder().id(2L).build()).build())
                .build();

        when(bookingRepository.findById(3L)).thenReturn(Optional.of(booking));

        BookingDto result = bookingService.getById(1L, 3L);

        assertThat(result).isNotNull();
    }

    @Test
    void getById_shouldThrowIfNoAccess() {
        Booking booking = Booking.builder()
                .id(3L)
                .booker(User.builder().id(1L).build())
                .item(Item.builder().owner(User.builder().id(2L).build()).build())
                .build();

        when(bookingRepository.findById(3L)).thenReturn(Optional.of(booking));

        assertThrows(NotFoundException.class,
                () -> bookingService.getById(99L, 3L));
    }

    @Test
    void getByBooker_shouldCallFindCurrent() {
        when(userRepository.findById(1L))
                .thenReturn(Optional.of(User.builder().id(1L).build()));

        when(bookingRepository.findCurrentByBooker(eq(1L), any(), any()))
                .thenReturn(new PageImpl<>(List.of()));

        bookingService.getByBooker(1L, BookingState.CURRENT, 0, 10);

        verify(bookingRepository)
                .findCurrentByBooker(eq(1L), any(), any());
    }

    @Test
    void getByOwner_shouldCallFindFuture() {
        when(userRepository.findById(2L))
                .thenReturn(Optional.of(User.builder().id(2L).build()));

        when(bookingRepository.findFutureByOwner(eq(2L), any(), any()))
                .thenReturn(new PageImpl<>(List.of()));

        bookingService.getByOwner(2L, BookingState.FUTURE, 0, 10);

        verify(bookingRepository)
                .findFutureByOwner(eq(2L), any(), any());
    }

}
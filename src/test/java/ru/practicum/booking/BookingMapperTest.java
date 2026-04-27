package ru.practicum.booking;

import org.junit.jupiter.api.Test;
import ru.practicum.item.Item;
import ru.practicum.item.ItemShortDto;
import ru.practicum.user.User;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class BookingMapperTest {
    @Test
    void toDto_shouldMapAllFieldsCorrectly() {
        User booker = User.builder()
                .id(1L)
                .name("John")
                .email("john@mail.com")
                .build();

        Item item = Item.builder()
                .id(10L)
                .name("Drill")
                .build();

        Booking booking = Booking.builder()
                .id(100L)
                .start(LocalDateTime.of(2024, 1, 1, 10, 0))
                .end(LocalDateTime.of(2024, 1, 2, 10, 0))
                .status(BookingStatus.APPROVED)
                .booker(booker)
                .item(item)
                .build();

        BookingDto dto = BookingMapper.toDto(booking);

        assertAll(
                () -> assertEquals(100L, dto.getId()),
                () -> assertEquals(LocalDateTime.of(2024, 1, 1, 10, 0), dto.getStart()),
                () -> assertEquals(LocalDateTime.of(2024, 1, 2, 10, 0), dto.getEnd()),
                () -> assertEquals(BookingStatus.APPROVED, dto.getStatus())
        );
    }

    @Test
    void toDto_shouldMapBookerCorrectly() {
        User booker = User.builder()
                .id(5L)
                .build();

        Item item = Item.builder()
                .id(10L)
                .name("Saw")
                .build();

        Booking booking = Booking.builder()
                .id(1L)
                .booker(booker)
                .item(item)
                .start(LocalDateTime.now())
                .end(LocalDateTime.now().plusDays(1))
                .status(BookingStatus.WAITING)
                .build();
        BookingDto dto = BookingMapper.toDto(booking);
        assertNotNull(dto.getBooker());
        assertEquals(5L, dto.getBooker().getId());
    }

    @Test
    void toDto_shouldMapItemCorrectly() {
        User booker = User.builder().id(1L).build();

        Item item = Item.builder()
                .id(99L)
                .name("Hammer")
                .build();

        Booking booking = Booking.builder()
                .id(1L)
                .booker(booker)
                .item(item)
                .start(LocalDateTime.now())
                .end(LocalDateTime.now().plusDays(1))
                .status(BookingStatus.REJECTED)
                .build();

        BookingDto dto = BookingMapper.toDto(booking);
        assertThat(dto.getItem())
                .isNotNull()
                .extracting(ItemShortDto::getId, ItemShortDto::getName)
                .containsExactly(99L, "Hammer");
    }

    @Test
    void toDto_shouldThrowException_whenBookerIsNull() {
        Item item = Item.builder().id(1L).name("Item").build();

        Booking booking = Booking.builder()
                .id(1L)
                .booker(null)
                .item(item)
                .start(LocalDateTime.now())
                .end(LocalDateTime.now().plusDays(1))
                .status(BookingStatus.WAITING)
                .build();
        assertThrows(NullPointerException.class,
                () -> BookingMapper.toDto(booking));
    }

    @Test
    void toDto_shouldThrowException_whenItemIsNull() {
        User booker = User.builder().id(1L).build();

        Booking booking = Booking.builder()
                .id(1L)
                .booker(booker)
                .item(null)
                .start(LocalDateTime.now())
                .end(LocalDateTime.now().plusDays(1))
                .status(BookingStatus.WAITING)
                .build();
        assertThrows(NullPointerException.class,
                () -> BookingMapper.toDto(booking));
    }
}
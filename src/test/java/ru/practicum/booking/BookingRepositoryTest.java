package ru.practicum.booking;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import ru.practicum.item.Item;
import ru.practicum.item.ItemRepository;
import ru.practicum.user.User;
import ru.practicum.user.UserRepository;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class BookingRepositoryTest {
    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Test
    @DisplayName("findAllByBooker должен возвращать бронирования пользователя")
    void findAllByBookerTest() {
        User user = userRepository.save(User.builder()
                .name("User")
                .email("user@mail.com")
                .build());

        Item item = itemRepository.save(Item.builder()
                .name("Item")
                .description("Desc")
                .available(true)
                .owner(user)
                .build());

        Booking booking = bookingRepository.save(Booking.builder()
                .booker(user)
                .item(item)
                .start(LocalDateTime.now().minusDays(1))
                .end(LocalDateTime.now().plusDays(1))
                .status(BookingStatus.APPROVED)
                .build());

        Page<Booking> result = bookingRepository.findAllByBooker(
                user.getId(),
                PageRequest.of(0, 10)
        );

        assertThat(result.getContent()).contains(booking);
    }

    @Test
    @DisplayName("findCurrentByBooker должен возвращать текущее бронирование")
    void findCurrentByBookerTest() {
        User user = userRepository.save(User.builder()
                .name("User")
                .email("user2@mail.com")
                .build());

        Item item = itemRepository.save(Item.builder()
                .name("Item")
                .description("Desc")
                .available(true)
                .owner(user)
                .build());

        Booking booking = bookingRepository.save(Booking.builder()
                .booker(user)
                .item(item)
                .start(LocalDateTime.now().minusHours(1))
                .end(LocalDateTime.now().plusHours(1))
                .status(BookingStatus.APPROVED)
                .build());

        Page<Booking> result = bookingRepository.findCurrentByBooker(
                user.getId(),
                LocalDateTime.now(),
                PageRequest.of(0, 10)
        );

        assertThat(result.getContent()).contains(booking);
    }

    @Test
    @DisplayName("findPastByBooker должен возвращать прошлые бронирования")
    void findPastByBookerTest() {
        User user = userRepository.save(User.builder()
                .name("User")
                .email("user3@mail.com")
                .build());

        Item item = itemRepository.save(Item.builder()
                .name("Item")
                .description("Desc")
                .available(true)
                .owner(user)
                .build());

        Booking booking = bookingRepository.save(Booking.builder()
                .booker(user)
                .item(item)
                .start(LocalDateTime.now().minusDays(5))
                .end(LocalDateTime.now().minusDays(3))
                .status(BookingStatus.APPROVED)
                .build());

        Page<Booking> result = bookingRepository.findPastByBooker(
                user.getId(),
                LocalDateTime.now(),
                PageRequest.of(0, 10)
        );

        assertThat(result.getContent()).contains(booking);
    }

    @Test
    @DisplayName("findFutureByBooker должен возвращать будущие бронирования")
    void findFutureByBookerTest() {
        User user = userRepository.save(User.builder()
                .name("User")
                .email("user4@mail.com")
                .build());

        Item item = itemRepository.save(Item.builder()
                .name("Item")
                .description("Desc")
                .available(true)
                .owner(user)
                .build());

        Booking booking = bookingRepository.save(Booking.builder()
                .booker(user)
                .item(item)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .status(BookingStatus.APPROVED)
                .build());

        Page<Booking> result = bookingRepository.findFutureByBooker(
                user.getId(),
                LocalDateTime.now(),
                PageRequest.of(0, 10)
        );

        assertThat(result.getContent()).contains(booking);
    }

    @Test
    @DisplayName("existsByBooker_IdAndItem_IdAndEndBeforeAndStatus должен возвращать true")
    void existsTest() {
        User user = userRepository.save(User.builder()
                .name("User")
                .email("user5@mail.com")
                .build());

        Item item = itemRepository.save(Item.builder()
                .name("Item")
                .description("Desc")
                .available(true)
                .owner(user)
                .build());

        Booking booking = bookingRepository.save(Booking.builder()
                .booker(user)
                .item(item)
                .start(LocalDateTime.now().minusDays(3))
                .end(LocalDateTime.now().minusDays(1))
                .status(BookingStatus.APPROVED)
                .build());

        boolean exists = bookingRepository.existsByBooker_IdAndItem_IdAndEndBeforeAndStatus(
                user.getId(),
                item.getId(),
                LocalDateTime.now(),
                BookingStatus.APPROVED
        );

        assertThat(exists).isTrue();
    }
}
package ru.practicum.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.item.Item;
import ru.practicum.item.ItemRepository;
import ru.practicum.user.User;
import ru.practicum.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class BookingServiceImplIntegrationTest {
    @Autowired
    BookingService bookingService;
    @Autowired
    UserRepository userRepository;

    @Autowired
    ItemRepository itemRepository;

    @Autowired
    BookingRepository bookingRepository;

    private User owner;
    private User booker;
    private Item item;

    @BeforeEach
    void setup() {
        bookingRepository.deleteAll();
        itemRepository.deleteAll();
        userRepository.deleteAll();

        owner = userRepository.save(User.builder()
                .name("Owner")
                .email("owner@mail.com")
                .build());

        booker = userRepository.save(User.builder()
                .name("Booker")
                .email("booker@mail.com")
                .build());

        item = itemRepository.save(Item.builder()
                .name("Laptop")
                .description("Good laptop")
                .available(true)
                .owner(owner)
                .build());
    }

    @Test
    void create_shouldSaveBookingToDatabase() {
        BookingCreateDto dto = BookingCreateDto.builder()
                .itemId(item.getId())
                .start(LocalDateTime.now().plusHours(1))
                .end(LocalDateTime.now().plusDays(1))
                .build();

        BookingDto result = bookingService.create(booker.getId(), dto);

        assertThat(result.getId()).isNotNull();

        List<Booking> bookings = bookingRepository.findAll();
        assertThat(bookings).hasSize(1);
        assertThat(bookings.get(0).getStatus())
                .isEqualTo(BookingStatus.WAITING);
    }

    @Test
    void approve_shouldChangeStatusInDatabase() {
        Booking booking = bookingRepository.save(Booking.builder()
                .item(item)
                .booker(booker)
                .start(LocalDateTime.now().plusHours(1))
                .end(LocalDateTime.now().plusDays(1))
                .status(BookingStatus.WAITING)
                .build());

        bookingService.approve(owner.getId(), booking.getId(), true);

        Booking updated = bookingRepository.findById(booking.getId()).get();

        assertThat(updated.getStatus())
                .isEqualTo(BookingStatus.APPROVED);
    }

    @Test
    void getById_shouldReturnBookingForOwner() {
        Booking booking = bookingRepository.save(Booking.builder()
                .item(item)
                .booker(booker)
                .start(LocalDateTime.now().plusHours(1))
                .end(LocalDateTime.now().plusDays(1))
                .status(BookingStatus.WAITING)
                .build());

        BookingDto result = bookingService.getById(owner.getId(), booking.getId());

        assertThat(result.getId()).isEqualTo(booking.getId());
    }

    @Test
    void getByBooker_shouldReturnBookings() {
        bookingRepository.save(Booking.builder()
                .item(item)
                .booker(booker)
                .start(LocalDateTime.now().plusHours(1))
                .end(LocalDateTime.now().plusDays(1))
                .status(BookingStatus.WAITING)
                .build());

        List<BookingDto> result = bookingService.getByBooker(
                booker.getId(),
                BookingState.ALL,
                0,
                10
        );

        assertThat(result).hasSize(1);
    }

    @Test
    void getByOwner_shouldReturnBookings() {
        bookingRepository.save(Booking.builder()
                .item(item)
                .booker(booker)
                .start(LocalDateTime.now().plusHours(1))
                .end(LocalDateTime.now().plusDays(1))
                .status(BookingStatus.WAITING)
                .build());

        List<BookingDto> result = bookingService.getByOwner(
                owner.getId(),
                BookingState.ALL,
                0,
                10
        );

        assertThat(result).hasSize(1);
    }

}
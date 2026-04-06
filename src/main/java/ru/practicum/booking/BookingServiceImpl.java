package ru.practicum.booking;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.item.Item;
import ru.practicum.item.ItemService;
import ru.practicum.user.User;
import ru.practicum.user.UserService;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Map;

@Service
public class BookingServiceImpl implements BookingService {
    private final UserService userService;
    private final ItemService itemService;

    private final Map<Long, Booking> bookings = new HashMap<>();
    private long nextId = 1;

    @Autowired
    public BookingServiceImpl(UserService userService, ItemService itemService) {
        this.userService = userService;
        this.itemService = itemService;
    }

    @Override
    public BookingDto create(BookingDto dto) {
        User booker = User.builder().id(dto.getBookerId()).build(); // минимально
        Item item = Item.builder().id(dto.getItemId()).build();
        Booking booking = Booking.builder()
                .id(nextId++)
                .booker(booker)
                .item(item)
                .start(dto.getStart())
                .end(dto.getEnd())
                .status("WAITING")
                .build();
        bookings.put(booking.getId(), booking);
        return BookingMapper.toBookingDto(booking);
    }

    @Override
    public BookingDto approve(Long bookingId, Long ownerId, boolean approved) {
        Booking booking = bookings.get(bookingId);
        if (booking == null) throw new RuntimeException("Бронирование не найдено");
        booking.setStatus(approved ? "APPROVED" : "REJECTED");
        return BookingMapper.toBookingDto(booking);
    }

    @Override
    public BookingDto getById(Long bookingId, Long userId) {
        Booking booking = bookings.get(bookingId);
        if (booking == null) throw new RuntimeException("Бронирование не найдено");
        return BookingMapper.toBookingDto(booking);
    }

    @Override
    public List<BookingDto> getAllByUser(Long userId) {
        return bookings.values().stream()
                .filter(b -> b.getBooker().getId().equals(userId))
                .map(BookingMapper::toBookingDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingDto> getAllByOwner(Long ownerId) {
        return bookings.values().stream()
                .filter(b -> b.getItem().getOwner().getId().equals(ownerId))
                .map(BookingMapper::toBookingDto)
                .collect(Collectors.toList());
    }
}

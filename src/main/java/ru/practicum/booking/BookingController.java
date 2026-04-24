package ru.practicum.booking;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bookings")
public class BookingController {
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";
    private final BookingService bookingService;

    @Autowired
    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public BookingDto create(@RequestHeader(USER_ID_HEADER) Long userId,
                             @RequestBody BookingCreateDto dto) {
        return bookingService.create(userId, dto);
    }

    @PatchMapping("/{bookingId}")
    public BookingDto approve(@PathVariable Long bookingId,
                              @RequestParam boolean approved,
                              @RequestHeader(USER_ID_HEADER) Long userId) {
        return bookingService.approve(userId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public BookingDto getById(@PathVariable Long bookingId,
                              @RequestHeader(USER_ID_HEADER) Long userId) {
        return bookingService.getById(userId, bookingId);
    }

    @GetMapping
    public List<BookingDto> getByBooker(@RequestHeader(USER_ID_HEADER) Long userId,
                                        @RequestParam(defaultValue = "ALL") BookingState state,
                                        @RequestParam(defaultValue = "0") int from,
                                        @RequestParam(defaultValue = "10") int size) {

        return bookingService.getByBooker(userId, state, from, size);
    }

    @GetMapping("/owner")
    public List<BookingDto> getByOwner(@RequestHeader(USER_ID_HEADER) Long userId,
                                       @RequestParam(defaultValue = "ALL") BookingState state,
                                       @RequestParam(defaultValue = "0") int from,
                                       @RequestParam(defaultValue = "10") int size) {

        return bookingService.getByOwner(userId, state, from, size);
    }
}

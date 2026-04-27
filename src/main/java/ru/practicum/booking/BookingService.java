package ru.practicum.booking;

import java.util.List;

public interface BookingService {
    BookingDto create(Long userId, BookingCreateDto dto);
    BookingDto approve(Long ownerId, Long bookingId, boolean approved);
    BookingDto getById(Long userId, Long bookingId);
    List<BookingDto> getByBooker(Long userId, BookingState state, int from, int size);
    List<BookingDto> getByOwner(Long userId, BookingState state, int from, int size);
}

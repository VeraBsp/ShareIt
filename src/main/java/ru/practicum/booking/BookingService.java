package ru.practicum.booking;

import java.util.List;

public interface BookingService {
    BookingDto create(BookingDto dto);

    BookingDto approve(Long bookingId, Long ownerId, boolean approved);

    BookingDto getById(Long bookingId, Long userId);

    List<BookingDto> getAllByUser(Long userId);

    List<BookingDto> getAllByOwner(Long ownerId);
}

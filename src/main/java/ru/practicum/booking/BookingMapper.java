package ru.practicum.booking;

import ru.practicum.item.ItemShortDto;

public class BookingMapper {
    public static BookingDto toDto(Booking booking) {
        return BookingDto.builder()
                .id(booking.getId())
                .start(booking.getStart())
                .end(booking.getEnd())
                .status(booking.getStatus())
                .booker(BookerDto.builder()
                        .id(booking.getBooker().getId())
                        .build())
                .item(ItemShortDto.builder()
                        .id(booking.getItem().getId())
                        .name(booking.getItem().getName())
                        .build())
                .build();
    }
}

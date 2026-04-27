package ru.practicum.item;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.booking.BookingShortDto;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemWithBookingDto {
    private Long id;
    private String name;
    private String description;
    private Boolean available;

    private BookingShortDto lastBooking;
    private BookingShortDto nextBooking;
    private List<CommentDto> comments = new ArrayList<>();;
}

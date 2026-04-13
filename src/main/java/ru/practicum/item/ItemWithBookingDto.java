package ru.practicum.item;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class ItemWithBookingDto {
    private Long id;
    private String name;
    private String description;
    private Boolean available;

    private LocalDateTime lastBooking;
    private LocalDateTime nextBooking;
    private List<CommentDto> comments = new ArrayList<>();;
}

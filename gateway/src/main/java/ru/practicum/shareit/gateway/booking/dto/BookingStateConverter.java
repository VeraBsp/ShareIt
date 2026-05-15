package ru.practicum.shareit.gateway.booking.dto;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class BookingStateConverter implements Converter<String, BookingState> {

    @Override
    public BookingState convert(String source) {
        return BookingState.valueOf(source.toUpperCase());
    }
}

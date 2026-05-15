package ru.practicum.shareit.gateway.booking.dto;

import ru.practicum.shareit.gateway.exception.BadRequestException;

public enum BookingState {
    // Все
    ALL,
    // Текущие
    CURRENT,
    // Будущие
    FUTURE,
    // Завершенные
    PAST,
    // Отклоненные
    REJECTED,
    // Ожидающие подтверждения
    WAITING;

    public static BookingState from(String stringState) {
        try {
            return BookingState.valueOf(stringState.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Unknown state: " + stringState);
        }
    }
}

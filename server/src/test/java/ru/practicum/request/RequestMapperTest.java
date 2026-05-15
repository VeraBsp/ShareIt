package ru.practicum.request;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.server.request.ItemRequest;
import ru.practicum.shareit.server.request.ItemRequestDto;
import ru.practicum.shareit.server.request.RequestMapper;
import ru.practicum.shareit.server.user.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RequestMapperTest {
    @Test
    void toDto_shouldMapBasicFields() {
        User requestor = User.builder()
                .id(10L)
                .name("User")
                .email("user@mail.com")
                .build();
        ItemRequest request = ItemRequest.builder()
                .id(100L)
                .description("Need drill")
                .requestor(requestor)
                .created(LocalDateTime.of(2024, 1, 1, 10, 0))
                .build();
        ItemRequestDto dto = RequestMapper.toDto(request);
        assertAll(
                () -> assertEquals(100L, dto.getId()),
                () -> assertEquals("Need drill", dto.getDescription()),
                () -> assertEquals(10L, dto.getRequestorId()),
                () -> assertEquals(LocalDateTime.of(2024, 1, 1, 10, 0), dto.getCreated())
        );
    }

    @Test
    void toDtoWithItems_shouldHandleEmptyList() {
        User requestor = User.builder().id(1L).build();
        ItemRequest request = ItemRequest.builder()
                .id(1L)
                .description("Empty request")
                .requestor(requestor)
                .created(LocalDateTime.now())
                .build();
        ItemRequestDto dto = RequestMapper.toDtoWithItems(request, List.of());
        assertNotNull(dto.getItems());
        assertTrue(dto.getItems().isEmpty());
    }

    @Test
    void toDto_shouldThrowException_whenRequestorIsNull() {
        ItemRequest request = ItemRequest.builder()
                .id(1L)
                .description("Bad request")
                .requestor(null)
                .created(LocalDateTime.now())
                .build();
        assertThrows(NullPointerException.class, () -> RequestMapper.toDto(request));
    }
}
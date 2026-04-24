package ru.practicum.request;

import org.junit.jupiter.api.Test;
import ru.practicum.item.Item;
import ru.practicum.item.ItemShortDto;
import ru.practicum.user.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class RequestMapperTest {
    @Test
    void toShortDto_shouldMapCorrectly() {
        Item item = Item.builder()
                .id(1L)
                .name("Drill")
                .build();
        ItemShortDto dto = RequestMapper.toShortDto(item);
        assertAll(
                () -> assertEquals(1L, dto.getId()),
                () -> assertEquals("Drill", dto.getName())
        );
    }

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
    void toDtoWithItems_shouldIncludeItemsList() {
        User requestor = User.builder().id(1L).build();
        ItemRequest request = ItemRequest.builder()
                .id(1L)
                .description("Need item")
                .requestor(requestor)
                .created(LocalDateTime.now())
                .build();
        Item item1 = Item.builder().id(11L).name("Hammer").build();
        Item item2 = Item.builder().id(22L).name("Saw").build();
        ItemRequestDto dto = RequestMapper.toDtoWithItems(request, List.of(item1, item2));
        assertThat(dto.getItems())
                .hasSize(2)
                .extracting(ItemShortDto::getName)
                .containsExactly("Hammer", "Saw");
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
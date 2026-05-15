package ru.practicum.item;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.dto.ItemDto;
import ru.practicum.shareit.server.item.Item;
import ru.practicum.shareit.server.item.ItemMapper;
import ru.practicum.shareit.server.request.ItemRequest;
import ru.practicum.shareit.server.user.User;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ItemMapperTest {
    @Test
    void toItemDto_shouldMapAllFields() {
        ItemRequest request = new ItemRequest();
        request.setId(10L);
        Item item = Item.builder()
                .id(1L)
                .name("Drill")
                .description("Power drill")
                .available(true)
                .request(request)
                .build();
        ItemDto dto = ItemMapper.toItemDto(item);
        assertEquals(1L, dto.getId());
        assertEquals("Drill", dto.getName());
        assertEquals("Power drill", dto.getDescription());
        assertEquals(true, dto.getAvailable());
        assertEquals(10L, dto.getRequestId());
    }

    @Test
    void toItemDto_shouldHandleNullRequest() {
        Item item = Item.builder()
                .id(2L)
                .name("Hammer")
                .description("Steel hammer")
                .available(false)
                .request(null)
                .build();
        ItemDto dto = ItemMapper.toItemDto(item);
        assertThat(dto.getId()).isEqualTo(2L);
        assertThat(dto.getRequestId()).isNull();
    }

    @Test
    void toItem_shouldMapAllFields() {
        ItemDto dto = ItemDto.builder()
                .name("Saw")
                .description("Wood saw")
                .available(true)
                .requestId(5L)
                .build();
        User owner = new User();
        owner.setId(1L);
        ItemRequest request = new ItemRequest();
        request.setId(5L);
        Item item = ItemMapper.toItem(dto, owner, request);
        assertThat(item.getName()).isEqualTo("Saw");
        assertThat(item.getDescription()).isEqualTo("Wood saw");
        assertThat(item.getAvailable()).isTrue();
        assertThat(item.getRequest()).isNotNull();
        assertThat(item.getRequest().getId()).isEqualTo(5L);
    }

    @Test
    void toItem_shouldSetDefaultAvailableTrue() {
        ItemDto dto = ItemDto.builder()
                .name("Knife")
                .description("Kitchen knife")
                .available(null)
                .build();
        User owner = new User();
        owner.setId(1L);
        Item item = ItemMapper.toItem(dto, owner, null);
        assertEquals(true, item.getAvailable());
    }

    @Test
    void toItem_shouldHandleNullRequestId() {
        ItemDto dto = ItemDto.builder()
                .name("Chair")
                .description("Wood chair")
                .available(true)
                .requestId(null)
                .build();

        User owner = new User();
        owner.setId(1L);
        Item item = ItemMapper.toItem(dto, owner, null);
        assertThat(item.getRequest()).isNull();
    }
}
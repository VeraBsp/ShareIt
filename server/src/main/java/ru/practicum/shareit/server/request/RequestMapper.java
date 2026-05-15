package ru.practicum.shareit.server.request;

import ru.practicum.shareit.dto.ItemDto;
import ru.practicum.shareit.server.item.Item;
import ru.practicum.shareit.server.item.ItemMapper;

import java.util.List;

public class RequestMapper {
    public static ItemRequestDto toDto(ItemRequest request) {
        return ItemRequestDto.builder()
                .id(request.getId())
                .description(request.getDescription())
                .requestorId(request.getRequestor().getId())
                .created(request.getCreated())
                .build();
    }

    public static ItemRequestDto toDtoWithItems(ItemRequest request, List<Item> items) {
        ItemRequestDto dto = toDto(request);

        List<ItemDto> itemDtos = items.stream()
                .map(ItemMapper::toItemDto)
                .toList();

        dto.setItems(itemDtos);
        return dto;
    }
}

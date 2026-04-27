package ru.practicum.request;

import ru.practicum.item.Item;
import ru.practicum.item.ItemShortDto;

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

        List<ItemShortDto> shortItems = items.stream()
                .map(RequestMapper::toShortDto)
                .toList();

        dto.setItems(shortItems);
        return dto;
    }

    public static ItemShortDto toShortDto(Item item) {
        return ItemShortDto.builder()
                .id(item.getId())
                .name(item.getName())
                .build();
    }
}

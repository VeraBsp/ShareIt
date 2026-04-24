package ru.practicum.item;

import ru.practicum.request.ItemRequest;

public class ItemMapper {
    public static ItemDto toItemDto(Item item) {
        return ItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .requestId(item.getRequest() != null ? item.getRequest().getId() : null)
                .build();
    }

    public static Item toItem(ItemDto dto) {
        Item item = Item.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .available(dto.getAvailable() != null ? dto.getAvailable() : true)
                .build();

        if (dto.getRequestId() != null) {
            ItemRequest request = new ItemRequest();
            request.setId(dto.getRequestId());
            item.setRequest(request);
        }
        return item;
    }
}

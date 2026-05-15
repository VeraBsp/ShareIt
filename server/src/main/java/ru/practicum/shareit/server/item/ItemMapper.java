package ru.practicum.shareit.server.item;

import ru.practicum.shareit.dto.ItemDto;
import ru.practicum.shareit.server.request.ItemRequest;
import ru.practicum.shareit.server.user.User;

public class ItemMapper {
    public static ItemDto toItemDto(Item item) {
        return new ItemDto(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getAvailable(),
                item.getRequest() != null ? item.getRequest().getId() : null
        );
    }

    public static Item toItem(ItemDto dto, User owner, ItemRequest request) {
        Item item = new Item();
        item.setName(dto.getName());
        item.setDescription(dto.getDescription());
        item.setAvailable(dto.getAvailable() != null ? dto.getAvailable() : true);
        item.setOwner(owner);
        item.setRequest(request);
        return item;
    }
}

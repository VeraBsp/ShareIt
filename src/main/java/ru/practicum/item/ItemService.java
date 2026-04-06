package ru.practicum.item;

import java.util.List;

public interface ItemService {
    ItemDto create(ItemDto itemDto, Long ownerId);

    ItemDto update(Long itemId, ItemDto itemDto, Long ownerId);

    ItemDto getById(Long itemId, Long userId);

    List<ItemDto> getAllByOwner(Long ownerId);

    List<ItemDto> search(String text);
}

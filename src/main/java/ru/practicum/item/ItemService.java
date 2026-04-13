package ru.practicum.item;

import java.util.List;

public interface ItemService {
    ItemDto create(ItemDto itemDto, Long ownerId);

    ItemDto update(Long itemId, ItemDto itemDto, Long ownerId);

    ItemWithBookingDto getById(Long itemId, Long userId);

    List<ItemWithBookingDto> getAllByOwner(Long ownerId);

    List<ItemDto> search(String text);

    CommentDto addComment(Long itemId, Long userId, CommentDto dto);
}

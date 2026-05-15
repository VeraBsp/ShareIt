package ru.practicum.shareit.server.item;

import ru.practicum.shareit.dto.CommentDto;
import ru.practicum.shareit.dto.ItemDto;

import java.util.List;

public interface ItemService {
    ItemDto create(ItemDto itemDto, Long ownerId);

    ItemDto update(Long itemId, ItemDto itemDto, Long ownerId);

    ItemWithBookingDto getById(Long itemId, Long userId);

    List<ItemWithBookingDto> getAllByOwner(Long ownerId, int from, int size);

    List<ItemDto> search(String text, int from, int size);

    CommentDto addComment(Long itemId, Long userId, CommentDto dto);
}

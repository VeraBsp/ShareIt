package ru.practicum.request;

import java.util.List;

public interface RequestService {
    ItemRequestDto create(ItemRequestDto dto, Long userId);

    ItemRequestDto getById(Long id, Long userId);

    List<ItemRequestDto> getAllByUser(Long userId);

    List<ItemRequestDto> getAll(Long userId, int from, int size);
}

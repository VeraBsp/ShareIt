package ru.practicum.shareit.server.request;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.server.exception.NotFoundException;
import ru.practicum.shareit.server.item.Item;
import ru.practicum.shareit.server.item.ItemRepository;
import ru.practicum.shareit.server.user.User;
import ru.practicum.shareit.server.user.UserRepository;

import java.util.*;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {
    private final ItemRequestRepository requestRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    public ItemRequestDto create(ItemRequestDto dto, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        ItemRequest request = ItemRequest.builder()
                .description(dto.getDescription())
                .requestor(user)
                .created(LocalDateTime.now())
                .build();

        return RequestMapper.toDto(requestRepository.save(request));
    }

    @Override
    public ItemRequestDto getById(Long id, Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        ItemRequest request = requestRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Запрос не найден"));
        List<Item> items = itemRepository.findByRequest_Id(request.getId());

        return RequestMapper.toDtoWithItems(request, items);
    }

    @Override
    public List<ItemRequestDto> getAllByUser(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        return requestRepository
                .findAllByRequestorIdOrderByCreatedDesc(userId)
                .stream()
                .map(r -> RequestMapper.toDtoWithItems(
                        r,
                        itemRepository.findByRequest_Id(r.getId())
                ))
                .toList();
    }

    @Override
    public List<ItemRequestDto> getAll(Long userId, int from, int size) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        int page = from / size;
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "created")
        );
        return requestRepository
                .findByRequestorIdNotOrderByCreatedDesc(userId, pageable)
                .stream()
                .map(r -> RequestMapper.toDtoWithItems(
                        r,
                        itemRepository.findByRequest_Id(r.getId())
                ))
                .toList();
    }
}

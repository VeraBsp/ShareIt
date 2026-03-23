package ru.practicum.request;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.user.User;
import ru.practicum.user.UserService;

import java.util.*;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Service
public class RequestServiceImpl implements RequestService {
    private final UserService userService;
    private final Map<Long, ItemRequest> requests = new HashMap<>();
    private long nextId = 1;

    @Autowired
    public RequestServiceImpl(UserService userService) {
        this.userService = userService;
    }

    @Override
    public ItemRequestDto create(ItemRequestDto dto, Long userId) {
        User user = User.builder().id(userId).build(); // минимально
        ItemRequest request = ItemRequest.builder()
                .id(nextId++)
                .description(dto.getDescription())
                .requestor(user)
                .created(LocalDateTime.now())
                .build();
        requests.put(request.getId(), request);
        return RequestMapper.toDto(request);
    }

    @Override
    public ItemRequestDto getById(Long id, Long userId) {
        ItemRequest request = requests.get(id);
        if (request == null) throw new RuntimeException("Запрос не найден");
        return RequestMapper.toDto(request);
    }

    @Override
    public List<ItemRequestDto> getAllByUser(Long userId) {
        return requests.values().stream()
                .filter(r -> r.getRequestor().getId().equals(userId))
                .map(RequestMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemRequestDto> getAll() {
        return requests.values().stream()
                .map(RequestMapper::toDto)
                .collect(Collectors.toList());
    }
}

package ru.practicum.request;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/requests")
public class RequestController {
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";
    private final RequestService requestService;

    @Autowired
    public RequestController(RequestService requestService) {
        this.requestService = requestService;
    }

    @PostMapping
    public ItemRequestDto create(@RequestHeader(USER_ID_HEADER) Long userId,
                                 @RequestBody ItemRequestDto requestDto) {
        return requestService.create(requestDto, userId);
    }

    @GetMapping
    public List<ItemRequestDto> getAllByUser(@RequestHeader(USER_ID_HEADER) Long userId) {
        return requestService.getAllByUser(userId);
    }

    @GetMapping("/all")
    public List<ItemRequestDto> getAll(        @RequestHeader(USER_ID_HEADER) Long userId,
                                               @RequestParam int from,
                                               @RequestParam int size) {
        return requestService.getAll(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public ItemRequestDto getById(@PathVariable Long requestId,
                                  @RequestHeader(USER_ID_HEADER) Long userId) {
        return requestService.getById(requestId, userId);
    }
}

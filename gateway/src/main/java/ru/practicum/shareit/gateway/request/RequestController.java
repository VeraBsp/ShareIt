package ru.practicum.shareit.gateway.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/requests")
@Validated
@Slf4j
public class RequestController {
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";
    private final RequestClient requestClient;

    @Autowired
    public RequestController(RequestClient requestClient) {
        this.requestClient = requestClient;
    }

    @PostMapping
    public ResponseEntity<Object> create(
            @RequestHeader(USER_ID_HEADER) Long userId,
            @Valid @RequestBody ItemRequestCreateDto body) {
        log.info("Пользователь с id={} создаёт новый запрос на вещь: {}", userId, body);
        return requestClient.create(userId, body);
    }

    @GetMapping
    public ResponseEntity<Object> getAllByUser(
            @RequestHeader(USER_ID_HEADER) Long userId) {
        log.info("Пользователь с id={} запрашивает список своих запросов на вещи", userId);
        return requestClient.getAllByUser(userId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getAll(
            @RequestHeader(USER_ID_HEADER) Long userId,
            @PositiveOrZero @RequestParam (defaultValue = "0") int from,
            @Positive @RequestParam (defaultValue = "10") int size) {
        log.info("Пользователь с id={} запрашивает список всех чужих запросов с пагинацией: from={}, size={}",
                userId, from, size);
        return requestClient.getAll(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> getById(
            @PathVariable Long requestId,
            @RequestHeader(USER_ID_HEADER) Long userId) {
        log.info("Пользователь с id={} запрашивает запрос на вещь с id={}", userId, requestId);
        return requestClient.getById(userId, requestId);
    }
}
